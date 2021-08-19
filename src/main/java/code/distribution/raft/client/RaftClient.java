package code.distribution.raft.client;

import code.distribution.raft.RaftConfig;
import code.distribution.raft.RaftClusterManager;
import org.apache.commons.lang3.RandomUtils;

/**
 * 〈客户端〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-06
 */
public class RaftClient {

    protected RpcClientService rpcClientService = new RpcClientService();

    protected String leader;

    public RaftClient(RaftConfig raftConfig){
        RaftClusterManager.config(raftConfig.parseClusterNodes());
    }

    public ClientRet invoke(ClientReq req) {
        return rpcClientService.invoke(getLeader(), req);
    }

    public ClientRet invoke(String nodeId, ClientReq req){
        return rpcClientService.invoke(nodeId, req);
    }

    public String getLeader(){
        if(leader == null){
            this.leader = lookupLeader();
        }
        return leader;
    }

    private String lookupLeader() {
        ClientReq req = new ClientReq(true, null);
        String node = randomNode();
        ClientRet ret = rpcClientService.invoke(node, req);
        if (ret == null) {
            return null;
        }
        if (ret.isSuccess()) {
            return node;
        } else if (ret.getLeaderId() != null) {
            return ret.getLeaderId();
        }
        return null;
    }

    public void refresh(){
        this.leader = lookupLeader();
        System.out.println("Leader is " + leader);
    }

    public String randomNode(){
        return RaftClusterManager.allNodes().get(RandomUtils.nextInt(0, RaftClusterManager.nodeNum()));
    }

}
