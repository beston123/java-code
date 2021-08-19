package code.distribution.raft.election;

import code.distribution.raft.ISender;
import code.distribution.raft.IService;
import code.distribution.raft.RaftClusterManager;
import code.distribution.raft.RaftNode;
import code.distribution.raft.enums.RoleType;
import code.distribution.raft.model.LogEntry;
import code.distribution.raft.model.RequestVoteReq;
import code.distribution.raft.model.RequestVoteRet;
import code.distribution.raft.rpc.RpcService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈请求投票 发送器〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019/3/11
 */
public class RequestVoteSender implements ISender<RequestVoteReq, RequestVoteRet>, IService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestVoteSender.class);

    private final RaftNode node;

    public RequestVoteSender(RaftNode node) {
        this.node = node;
    }

    @Override
    public void start() {

    }

    public List<RequestVoteRet> broadcastRequestVote() {
        return broadcastRequestVote(node.currentTerm(), false);
    }

    public List<RequestVoteRet> broadcastRequestVote(int currentTerm, boolean preVote) {
        List<RequestVoteRet> requestVoteRets = new ArrayList<>();
        String myNodeId = node.getNodeId();

        int lastLogIndex = -1;
        int lastLogTerm = -1;
        Pair<Integer, LogEntry> lastLogPair = node.getLogModule().lastLog();
        if (lastLogPair != null) {
            lastLogIndex = lastLogPair.getLeft();
            lastLogTerm = lastLogPair.getRight().getTerm();
        }
        RequestVoteReq req = RequestVoteReq.build(currentTerm, myNodeId, lastLogIndex, lastLogTerm, preVote);

        RaftClusterManager.otherNodes().parallelStream().forEach(nodeId -> {
            if (node.getRole() == RoleType.CANDIDATE || preVote) {
                LOGGER.debug("Send vote request to {}", nodeId);
                RequestVoteRet ret = send(nodeId, req);
                if (ret != null) {
                    requestVoteRets.add(ret);
                }
            }
        });
        return requestVoteRets;
    }

    @Override
    public void close() {

    }

    @Override
    public RequestVoteRet send(String nodeId, RequestVoteReq requestVoteReq) {
        return RpcService.getInstance().requestVote(nodeId, requestVoteReq);
    }
}
