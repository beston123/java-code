package code.distribution.raft.election;

import code.distribution.raft.IHandler;
import code.distribution.raft.RaftNode;
import code.distribution.raft.RaftNodeServer;
import code.distribution.raft.model.LogEntry;
import code.distribution.raft.model.RequestVoteReq;
import code.distribution.raft.model.RequestVoteRet;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈请求投票 处理器〉<p>
 * 接收者的实现逻辑
 * <p>
 * 1、如果收到的任期比当前任期小，返回false
 * <p>
 * 2、如果本地状态中votedFor为null或者candidateId，且candidate的日志等于或多余（按照index判断）接收者的日志，
 * 则接收者投票给candidate，即返回true
 * <p>
 * PS：Raft 通过比较两份日志中最后一条日志条目的索引值和任期号定义谁的日志比较新。
 * 如果两份日志最后的条目的任期号不同，那么任期号大的日志更加新。如果两份日志最后的条目任期号相同，那么日志比较长的那个就更加新。
 *
 * @author zixiao
 * @date 2019/3/11
 */
public class RequestVoteHandler implements IHandler<RequestVoteReq, RequestVoteRet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestVoteHandler.class);

    private final RaftNode node;

    private final RaftNodeServer nodeServer;

    public RequestVoteHandler(RaftNode node, RaftNodeServer nodeServer) {
        this.node = node;
        this.nodeServer = nodeServer;
    }

    @Override
    public RequestVoteRet handle(RequestVoteReq requestVoteReq) {
        if (requestVoteReq.isPreVote()) {
            //预投票
            return preVote(requestVoteReq);
        } else {
            //正式投票
            return requestVote(requestVoteReq);
        }
    }

    /**
     * 预投票
     * @param requestVoteReq
     * @return
     */
    private RequestVoteRet preVote(RequestVoteReq requestVoteReq){
        int currentTerm = node.currentTerm();
        int candidateTerm = requestVoteReq.getTerm();
        if (candidateTerm < currentTerm) {
            LOGGER.info("PreVote Reject: candidateTerm({}) < currentTerm({}).", candidateTerm, currentTerm);
            return RequestVoteRet.reject(currentTerm);
        }
//        if (node.elapsedLastRpcTime() <= RaftConst.HEARTBEAT_MS) {
//            return RequestVoteRet.reject(currentTerm);
//        }
        if(node.canVoteFor(requestVoteReq.getCandidateId(), candidateTerm)){
            LOGGER.info("PreVote Accept: " + requestVoteReq);
            return RequestVoteRet.accept(currentTerm);
        }else{
            LOGGER.info("PreVote Reject: I have voted for {}", node.getVoteFor());
            return RequestVoteRet.reject(currentTerm);
        }
    }

    /**
     * 接收者的实现逻辑
     * <p>
     * 1、如果收到的任期比当前任期小，返回false
     * 2、如果本地状态中votedFor为null或者candidateId，且candidate的日志等于或多余（按照index判断）接收者的日志，
     * 则接收者投票给candidate，即返回true
     *
     * @param requestVoteReq
     * @return
     */
    private synchronized RequestVoteRet requestVote(RequestVoteReq requestVoteReq) {
        //1.1如果收到的任期比当前任期小，返回false
        int currentTerm = node.currentTerm();
        int candidateTerm = requestVoteReq.getTerm();
        if (candidateTerm < currentTerm) {
            return RequestVoteRet.reject(currentTerm);
        //1.2如果RPC请求或者响应包含的任期T > currentTerm，将currentTerm设置为T，并转换为Follower
        } else if (candidateTerm > currentTerm) {
            nodeServer.changeToFollower(currentTerm, candidateTerm);
            currentTerm = candidateTerm;
        }

        //2、如果本地状态中votedFor为null或者candidateId，且candidate的日志等于或多余（按照index判断）接收者的日志，则接收者投票给candidate，即返回true
        if (node.canVoteFor(requestVoteReq.getCandidateId(), candidateTerm)) {
            Pair<Integer, LogEntry> currentLastLog = node.getLogModule().lastLog();
            //2.1 candidate的日志 数量多
            if (currentLastLog == null || requestVoteReq.getLastLogIndex() > currentLastLog.getLeft()) {
                return tryVoteFor(requestVoteReq.getCandidateId(), candidateTerm);
                //2.2 candidate的日志 数量相等，比较日志新旧
            } else if (requestVoteReq.getLastLogIndex() == currentLastLog.getLeft()) {
                // 如果两份日志最后的条目的任期号不同，那么任期号大的日志更加新。
                if (requestVoteReq.getLastLogTerm() >= currentLastLog.getRight().getTerm()) {
                    return tryVoteFor(requestVoteReq.getCandidateId(), candidateTerm);
                }
            }
            LOGGER.info("Vote Reject: Candidate's log is older than me, {}", requestVoteReq);
        } else {
            LOGGER.info("Vote Reject: I have voted for candidate {}", node.getVoteFor());
        }
        return RequestVoteRet.reject(currentTerm);
    }

    /**
     * 尝试投票
     *
     * @param candidateId
     * @param candidateTerm
     * @return
     */
    private RequestVoteRet tryVoteFor(String candidateId, int candidateTerm) {
        //加锁投票
        if (node.voteFor(candidateId, candidateTerm)) {
            LOGGER.info("Vote Accept: I vote for [candidate={} term={}] success.", candidateId, candidateTerm);
            //重置选举定时器
            nodeServer.resetElectionTimeout();
            return RequestVoteRet.accept(candidateTerm);
        } else {
            LOGGER.info("Vote Reject: Try vote for [candidate={} term={}], but fail.", candidateId, candidateTerm);
            return RequestVoteRet.reject(node.currentTerm());
        }
    }

}
