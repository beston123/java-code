package code.distribution.raft;

import code.distribution.raft.enums.RoleType;
import code.distribution.raft.fsm.StateMachine;
import code.distribution.raft.log.LogModule;
import code.distribution.raft.model.LogEntry;
import code.distribution.raft.model.VoteFor;
import code.distribution.raft.util.SnapshotUtils;
import code.util.NamedThreadFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 〈Raft节点〉<p>
 *
 状态	所有服务器上持久存在的
 currentTerm	服务器最后一次知道的任期号（初始化为 0，持续递增）
 votedFor	在当前获得选票的候选人的 Id
 log[]	日志条目集；每一个条目包含一个用户状态机执行的指令，和收到时的任期号
 *
 状态	所有服务器上经常变的
 commitIndex	已知的最大的已经被提交的日志条目的索引值
 lastApplied	最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
 *
 状态	在领导人里经常改变的 （选举后重新初始化）
 nextIndex[]	对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一）
 matchIndex[]	对于每一个服务器，已经复制给他的日志的最高索引值
 *
 * @author zixiao
 * @date 2019/3/11
 */
@Getter
@ToString
@EqualsAndHashCode
public class RaftNode implements Serializable{

    /**
     * 唯一标识
     */
    private final String nodeId;

    /**
     * 角色
     */
    private RoleType role;

    /**
     * 服务器最后一次知道的任期号
     * 初始化为 0，持续递增
     */
    private AtomicInteger currentTerm;

    /**
     * 在当前获得选票的候选人的 Id
     * 投给谁
     */
    private VoteFor voteFor;

    /**
     * 日志条目集；
     * 每一个条目包含一个用户状态机执行的指令，和收到时的任期号
     */
    private LogModule logModule;

    /**
     * 状态机
     */
    private StateMachine stateMachine;

    /**
     * 已知的最大的已经被提交的日志条目的索引值
     */
    private transient AtomicInteger commitIndex;

    /**
     * 最后被应用到状态机的日志条目索引值
     * 初始化为 0，持续递增
     */
    private transient AtomicInteger lastApplied;

    /**
     * 对于每一个服务器，需要发送给他的下一个日志条目的索引值
     * 初始化为领导人最后索引值加一
     */
    private transient Map<String, Integer> nextIndex = new HashMap<>(32);

    /**
     * 对于每一个服务器，已经复制给他的日志的最高索引值
     */
    private transient Map<String, Integer> matchIndex = new HashMap<>(32);

    /**
     * 当前leaderId
     */
    private String leaderId;

    /**
     * 上次接受leaderRpc请求时间
     */
    private AtomicLong lastRpcTimestamp = new AtomicLong(0);

    /**
     * 日志应用到状态机线程
     */
    private ThreadPoolExecutor logApplyExecutor = new ThreadPoolExecutor(1, 1,
            5, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1024),
            new NamedThreadFactory("RetryAppendEntriesTimer-"),
            new ThreadPoolExecutor.CallerRunsPolicy());


    private transient ReentrantLock voteLock = new ReentrantLock();

    public RaftNode(String nodeId, StateMachine stateMachine) {
        this.nodeId = nodeId;
        this.stateMachine = stateMachine;

        this.role = RoleType.FOLLOWER;
        this.currentTerm = new AtomicInteger(0);
        this.logModule = new LogModule();
        this.commitIndex = new AtomicInteger(-1);
        this.lastApplied = new AtomicInteger(-1);
    }

    public int currentTerm(){
        return this.currentTerm.get();
    }

    /**
     * 距离上次leaderRpc更新的时间差
     * @return
     */
    public long elapsedLastRpcTime(){
        return System.currentTimeMillis() - lastRpcTimestamp.get();
    }

    protected boolean compareAndSetTerm(int curTerm, int newTerm){
        return currentTerm.compareAndSet(curTerm, newTerm);
    }

    public void setCommitIndex(int value){
        this.commitIndex.set(value);
    }

    public void setLeader(String leaderId){
        this.leaderId = leaderId;
    }

    /**
     * 投票给候选人
     * @param candidateId
     * @param term
     * @return
     */
    public boolean voteFor(String candidateId, int term){
        voteLock.lock();
        try {
            boolean success = canVoteFor(candidateId, term);
            if(success){
                voteFor = new VoteFor(candidateId, term);
            }
            return success;
        }finally {
            voteLock.unlock();
        }
    }

    /**
     * 是否可以投票给候选人
     * 条件：只有未投票，或者term比已投候选人的term更大时，可以投票
     * 保证在同一个term中，只能投给一个候选人
     * @param candidateId
     * @param candidateTerm
     * @return
     */
    public boolean canVoteFor(String candidateId, int candidateTerm){
        if(voteFor == null){
            return true;
        }else if(voteFor.getTerm() < candidateTerm){
            return true;
        }else if(voteFor.getTerm() == candidateTerm && voteFor.getNodeId().equals(candidateId)){
            return true;
        }
        return false;
    }

    /*************************************** role change ***************************************/

    protected void changeToCandidate(){
        if(role == RoleType.FOLLOWER){
            role = RoleType.CANDIDATE;
        }
    }

    protected void changeToLeader(){
        if(role == RoleType.CANDIDATE){
            role = RoleType.LEADER;
            initIndex();
        }
    }

    protected void changeToFollower(){
        role = RoleType.FOLLOWER;
    }

    /*************************************** logEntry ***************************************/

    private void initIndex(){
        // 每个follower的日志待发送位置初始化为leader最后日志位置+1
        int initNextIndex = logModule.lastLogIndex() + 1;
        nextIndex.clear();
        matchIndex.clear();

        RaftClusterManager.otherNodes().forEach(nodeId -> {
            nextIndex.put(nodeId, initNextIndex);
            matchIndex.put(nodeId, -1);
        });
        commitIndex.set(-1);
    }

    public void addNodeIndex(String nodeId){
        if (!nextIndex.containsKey(nodeId)) {
            nextIndex.put(nodeId, 0);
            matchIndex.put(nodeId, -1);
        }
    }

    public void removeNodeIndex(String nodeId){
        if (nextIndex.containsKey(nodeId)) {
            nextIndex.remove(nodeId);
            matchIndex.remove(nodeId);
        }
    }

    protected void saveSnapshot(){
        SnapshotUtils.save(this);
    }

    /*************************************** state machine ***************************************/

    /**
     * 应用日志到状态机，直到commitIndex位置位置（异步）
     * [lastApplied+1，commitIndex]
     *
     * @param commitIdx
     */
    public Future<Boolean> applyTo(int commitIdx){
        return logApplyExecutor.submit(() -> applyToSync(commitIdx));
    }

    /**
     * 应用日志到状态机，直到commitIndex位置位置
     *
     * @param commitIdx
     * @return lastApplied >= commitIdx 返回true
     */
    public boolean applyToSync(int commitIdx){
        synchronized (stateMachine) {
            int startApplyIndex = lastApplied.get() + 1;
            if (startApplyIndex > commitIdx) {
                return true;
            }
            List<LogEntry> toApplyLogs = logModule.subLogs(startApplyIndex, commitIdx);
            toApplyLogs.forEach(logEntry -> {
                stateMachine.apply(logEntry);
                lastApplied.incrementAndGet();
            });
        }
        return lastApplied.get() >= commitIdx;
    }

}
