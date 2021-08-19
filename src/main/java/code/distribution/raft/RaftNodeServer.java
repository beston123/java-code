package code.distribution.raft;

import code.distribution.raft.client.ClientReq;
import code.distribution.raft.client.ClientRet;
import code.distribution.raft.election.ElectionService;
import code.distribution.raft.election.RequestVoteHandler;
import code.distribution.raft.enums.RoleType;
import code.distribution.raft.fsm.StateMachine;
import code.distribution.raft.log.AppendEntriesHandler;
import code.distribution.raft.log.AppendEntriesSender;
import code.distribution.raft.model.*;
import code.distribution.raft.rpc.HttpNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈Raft节点服务器〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019/3/11
 */
public class RaftNodeServer implements IService{

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftNodeServer.class);

    private final RaftNode node;

    private final IHandler<RequestVoteReq, RequestVoteRet> requestVoteHandler;

    private final IHandler<AppendEntriesReq, AppendEntriesRet> appendEntriesHandler;

    private final ElectionService electionService;

    private final AppendEntriesSender appendEntriesSender;

    private final HttpNettyServer httpNettyServer;

    private final NodeManageService nodeManageService;

    public RaftNodeServer(String nodeId, StateMachine stateMachine) {
        this.node = new RaftNode(nodeId, stateMachine);
        this.requestVoteHandler = new RequestVoteHandler(node, this);
        this.appendEntriesHandler = new AppendEntriesHandler(node, this);
        this.electionService = new ElectionService(node, this);
        this.appendEntriesSender = new AppendEntriesSender(node, this);
        this.httpNettyServer = new HttpNettyServer(this);
        this.nodeManageService = new NodeManageService(this);
    }

    public void initConfig(RaftConfig raftConfig){
        RaftClusterManager.config(raftConfig);
    }

    @Override
    public void start() {
        LOGGER.info("Node {} start...", node.getNodeId());
        electionService.start();
        //放最后，等预热后再接受请求
        httpNettyServer.start();
    }

    @Override
    public void close() {
        LOGGER.info("Node {} stop...", node.getNodeId());
        electionService.close();
        appendEntriesSender.close();
        nodeManageService.close();
        httpNettyServer.destroy();
        //保存快照
        node.saveSnapshot();
    }

    public RequestVoteRet handleRequestVote(RequestVoteReq req){
        return requestVoteHandler.handle(req);
    }

    public AppendEntriesRet handleAppendEntries(AppendEntriesReq req){
        return appendEntriesHandler.handle(req);
    }

    public ClientRet handleClientRequest(ClientReq clientReq){
        if (node.getRole() == RoleType.FOLLOWER) {
            return ClientRet.buildRedirect(node.getLeaderId());
        } else if (node.getRole() == RoleType.CANDIDATE) {
            return ClientRet.buildRedirect(null);
        }

        //lookup leader请求
        if (clientReq.getCommand() == null) {
            return ClientRet.buildSuccess(null);
        }

        if (clientReq.isRead()) {
            String key = ((BaseCommand) clientReq.getCommand()).getKey();
            Object value = node.getStateMachine().get(key);
            return ClientRet.buildSuccess(value);
        } else {
            boolean appendSuccess = appendEntriesSender.appendEntries(clientReq.getCommand());
            return ClientRet.build(appendSuccess);
        }
    }

    public void resetElectionTimeout(){
        this.electionService.resetElectionTimeout();
    }

    public void changeToCandidate(){
        node.setLeader(null);
        node.changeToCandidate();
    }

    public void changeToLeader(){
        node.changeToLeader();
        appendEntriesSender.start();
        nodeManageService.start();
        electionService.pauseElection();
    }

    public boolean changeToFollower(int currentTerm, int newTerm){
        if(node.compareAndSetTerm(currentTerm, newTerm)){
            if(RoleType.LEADER == node.getRole()){
                LOGGER.info("Leader[term={}] step down, newTerm={}", currentTerm, newTerm);
                appendEntriesSender.close();
                nodeManageService.close();
            }
            node.changeToFollower();
            return true;
        }
        return false;
    }

    /**
     * 接受leader的Rpc请求
     * 1、设置leaderId
     * 2、更新上次rpc时间戳
     * 3、重置选举超时时间
     * @param leaderId
     */
    public void acceptLeaderRpc(String leaderId){
        node.setLeader(leaderId);
        node.getLastRpcTimestamp().set(System.currentTimeMillis());
        resetElectionTimeout();
    }

    public RaftNode getNode() {
        return node;
    }

    public void addNode(String nodeId){
        node.addNodeIndex(nodeId);
        appendEntriesSender.addToAppend(nodeId);
    }

    public void removeNode(String nodeId){
        node.removeNodeIndex(nodeId);
    }

}
