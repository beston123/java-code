package code.distribution.raft.client;

import code.distribution.raft.enums.RequestType;
import code.distribution.raft.rpc.RaftRequest;
import code.distribution.raft.rpc.RaftResponse;
import code.distribution.raft.rpc.RpcHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈Rpc客户端服务〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-06
 */
public class RpcClientService extends RpcHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientService.class);

    public ClientRet invoke(String nodeId, ClientReq req) {
        RaftResponse raftResponse = post(nodeId, new RaftRequest(RequestType.CLIENT_REQ, req));
        if (raftResponse.isSuccess()) {
            LOGGER.debug("Invoke raft server to {} success, {}", nodeId, raftResponse.getResponse());
            return (ClientRet) raftResponse.getResponse();
        }
        LOGGER.error("Invoke raft server to {} fail {}", nodeId, raftResponse.getErrorMsg());
        return null;
    }
}
