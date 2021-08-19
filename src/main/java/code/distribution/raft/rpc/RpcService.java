package code.distribution.raft.rpc;

import code.distribution.raft.enums.RequestType;
import code.distribution.raft.model.AppendEntriesReq;
import code.distribution.raft.model.AppendEntriesRet;
import code.distribution.raft.model.RequestVoteReq;
import code.distribution.raft.model.RequestVoteRet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈RpcService〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019/3/7
 */
public class RpcService extends RpcHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcService.class);

    private static RpcService INST = new RpcService();

    private RpcService() {
        super();
    }

    public static RpcService getInstance() {
        return INST;
    }

    public RequestVoteRet requestVote(String nodeId, RequestVoteReq req) {
        RaftResponse raftResponse = post(nodeId, new RaftRequest(RequestType.REQUEST_VOTE, req));
        if (raftResponse.isSuccess()) {
            LOGGER.debug("Vote request to {} success, {}", nodeId, raftResponse.getResponse());
            return (RequestVoteRet) raftResponse.getResponse();
        }
        LOGGER.error("Vote request to {} fail, {}", nodeId, raftResponse.getErrorMsg());
        return null;
    }

    public AppendEntriesRet appendEntries(String nodeId, AppendEntriesReq req) {
        RaftResponse raftResponse = post(nodeId, new RaftRequest(RequestType.APPEND_ENTRIES, req));
        if (raftResponse.isSuccess()) {
            LOGGER.debug("Append entries to {} success, {}", nodeId, raftResponse.getResponse());
            return (AppendEntriesRet) raftResponse.getResponse();
        }
        LOGGER.error("Append entries to {} fail, {}", nodeId, raftResponse.getErrorMsg());
        return null;
    }

}
