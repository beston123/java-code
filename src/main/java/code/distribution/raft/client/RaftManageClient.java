package code.distribution.raft.client;

import code.distribution.raft.RaftConfig;
import code.distribution.raft.model.NodeCommand;
import lombok.extern.slf4j.Slf4j;

/**
 * 〈Raft节点管理客户端〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/08
 */
@Slf4j
public class RaftManageClient extends RaftClient {

    public RaftManageClient(RaftConfig raftConfig) {
        super(raftConfig);
    }

    public boolean addNode(String nodeId){
        NodeCommand command = NodeCommand.buildAdd(nodeId);
        return manageNode(command);
    }

    public boolean removeNode(String nodeId){
        NodeCommand command = NodeCommand.buildRemove(nodeId);
        return manageNode(command);
    }

    private boolean manageNode(NodeCommand command) {
        ClientReq clientReq = new ClientReq(false, command);
        ClientRet clientRet = invoke(clientReq);
        if (clientRet.isSuccess()) {
            return true;
        } else if (clientRet.getLeaderId() != null) {
            log.info("Request redirect, please try again. leaderId={}, ", clientRet.getLeaderId());
        }
        return false;
    }

}
