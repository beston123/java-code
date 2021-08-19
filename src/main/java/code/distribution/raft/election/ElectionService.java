package code.distribution.raft.election;

import code.distribution.raft.*;
import code.distribution.raft.enums.RoleType;
import code.distribution.raft.model.RequestVoteRet;
import code.util.NamedThreadFactory;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈选举服务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019/3/11
 */
public class ElectionService implements IService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionService.class);

    private final RaftNode node;

    private final RaftNodeServer nodeServer;

    private final ScheduledThreadPoolExecutor electionTimer;

    private final AtomicBoolean inElection = new AtomicBoolean();

    private long electionTimeOut;

    /**
     * 是否暂停
     */
    private boolean pause = false;

    private final RequestVoteSender requestVoteSender;

    public ElectionService(RaftNode node, RaftNodeServer nodeServer) {
        this.node = node;
        this.nodeServer = nodeServer;
        this.electionTimer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("ElectionTimer-"), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                executor.getQueue().poll();
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.requestVoteSender = new RequestVoteSender(node);
    }

    @Override
    public void start() {
        electionTimeOut = getRandomElectionTimeout();
        this.electionTimer.scheduleWithFixedDelay(new ElectionTask(), RaftConst.SCHEDULER_DELAY_MS + electionTimeOut, electionTimeOut, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        pauseElection();
        requestVoteSender.close();
    }

    public void resetElectionTimeout() {
        this.electionTimer.getQueue().clear();
        this.pause = false;

        electionTimeOut = getRandomElectionTimeout();
        this.electionTimer.scheduleWithFixedDelay(new ElectionTask(), electionTimeOut, electionTimeOut, TimeUnit.MILLISECONDS);
    }

    private long getRandomElectionTimeout() {
        return RaftConst.ELECTION_TIMEOUT_MS + RandomUtils.nextLong(0, RaftConst.ELECTION_TIMEOUT_MS);
    }

    /**
     * 一、预投票
     * 二、正式开始投票选举
     * 1、当前term自增1
     * 2、身份切换为Candidate
     * 3、投票给自己
     * 4、广播投票, 发送 RequestVote 消息(带上currentTerm)给其它所有server
     */
    private class ElectionTask implements Runnable {

        @Override
        public void run() {
            while (node.getRole() == RoleType.FOLLOWER){
                try {
                    if (pause || !inElection.compareAndSet(false, true)) {
                        return;
                    }
                    //如果距离上次leaderRpc更新的时间差 >= 选举超时时间，则尝试选举
                    if (node.elapsedLastRpcTime() >= electionTimeOut) {
                        //先预投票，得到多数票后在正式发起投票选举
                        int voteTerm = node.currentTerm() + 1;
                        if (preVoteSuccess(voteTerm)) {
                            startVote();
                        } else {
                            LOGGER.info("Pre vote fail, cannot get majority votes, voteTerm={}", voteTerm);
                        }
                        break;
                    } else {
                        Thread.sleep(RaftConst.SLEEP_DEVIATION_MS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    inElection.compareAndSet(true, false);
                }
            }
        }
    }

    /**
     * 预投票
     * @return
     */
    private boolean preVoteSuccess(int voteTerm){
        List<RequestVoteRet> voteRetList = requestVoteSender.broadcastRequestVote(voteTerm, true);

        //处理预投票结果
        int preVotes = 1;
        for (RequestVoteRet requestVoteRet : voteRetList) {
            if (requestVoteRet != null && requestVoteRet.isVoteGranted()) {
                preVotes++;
            }
        }
        //多数派同意
        return preVotes > (RaftClusterManager.nodeNum() / 2);
    }

    /**
     * 发起正式投票选举
     */
    private void startVote(){
        //1、当前term自增1
        int currentTerm = node.getCurrentTerm().incrementAndGet();
        LOGGER.info("start election, term={}", currentTerm);

        //2、身份切换为Candidate
        nodeServer.changeToCandidate();

        //3、投票给自己
        boolean success = node.voteFor(node.getNodeId(), currentTerm);

        //4、广播投票, 发送 RequestVote 消息(带上currentTerm)给其它所有server
        if (success) {
            LOGGER.info("start broadcast RequestVote, term={}", currentTerm);
            List<RequestVoteRet> voteRetList = requestVoteSender.broadcastRequestVote();

            //处理投票结果
            handleVoteResult(voteRetList);
        } else {
            //被其他candidate捷足先登了
            LOGGER.info("Can not vote to self, term={}", currentTerm);
        }
    }

    private void handleVoteResult(List<RequestVoteRet> voteRetList) {
        int votesNum = 0;
        int maxTerm = node.currentTerm();
        for (RequestVoteRet requestVoteRet : voteRetList) {
            if (requestVoteRet == null) {
                //Vote rpc fail
                continue;
            }
            if (requestVoteRet.isVoteGranted()) {
                votesNum++;
            } else if (requestVoteRet.getTerm() > node.currentTerm()) {
                maxTerm = requestVoteRet.getTerm();
            }
        }
        //如果RPC请求或者响应包含的任期T > currentTerm，将currentTerm设置为T并转换为Follower
        if (maxTerm > node.currentTerm()) {
            LOGGER.info("Some node's term ({}) is greater then mime (term={}).", maxTerm, node.getCurrentTerm());
            nodeServer.changeToFollower(node.currentTerm(), maxTerm);
            return;
        }
        //是否任然投给自己
        if (node.getVoteFor() != null && node.getNodeId().equals(node.getVoteFor().getNodeId())) {
            votesNum++;
        }
        //多数派原则，当选leader
        if (votesNum > RaftClusterManager.nodeNum() / 2) {
            LOGGER.info("I get majority votes ({}) and become the leader in term {}.", votesNum, node.currentTerm());
            nodeServer.changeToLeader();
        } else {
            LOGGER.info("I get minority votes ({})，less than nodesNum/2+1({}).", votesNum, RaftClusterManager.nodeNum()/2+1);
        }
    }

    /**
     * 暂停选举服务，（当选leader后）
     */
    public void pauseElection() {
        pause = true;
    }

}
