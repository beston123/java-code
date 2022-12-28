package code.distribution.raft;

import code.distribution.raft.model.NodeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 〈集群管理〉<p>
 * 〈维护集群节点的上下线〉
 *
 * @author zixiao
 * @date 2019/3/7
 */
public class RaftClusterManager {

    /**
     * 所有节点
     */
    private static Set<String> allNodes = new HashSet<>();

    /**
     * 除自身外的索引节点
     */
    private static Set<String> otherNodes = new HashSet<>();

    /**
     * 新变更节点队列
     */
    private static BlockingQueue<NodeEvent> newNodeQueue = new LinkedBlockingQueue<>();

    /**
     * 自身节点
     * 如果有值，则本节点是raft服务端
     * 如果为null，则本节点是raft客户端
     */
    private static String selfId;

    public static void config(String[] nodeIds){
        for (String nodeId : nodeIds) {
            allNodes.add(nodeId);
        }
    }

    public static void config(RaftConfig raftConfig){
        config(raftConfig.parseClusterNodes());
        if(raftConfig.getNodeId() != null){
            selfId = raftConfig.getNodeId();
            otherNodes = allNodes.stream().filter(nodeId -> !nodeId.equals(selfId)).collect(Collectors.toSet());
        }
    }

    public static List<String> allNodes(){
        return new ArrayList<>(allNodes);
    }

    public static Set<String> otherNodes(){
        return otherNodes;
    }

    public static BlockingQueue<NodeEvent> newNodeQueue(){
        return newNodeQueue;
    }

    public static int nodeNum(){
       return allNodes.size();
    }

    public static boolean exist(String nodeId){
        return allNodes().contains(nodeId);
    }

    public static boolean addNode(String nodeId){
        synchronized (allNodes) {
            //是否为raft服务端
            if (selfId != null && !nodeId.equals(selfId)) {
                otherNodes.add(nodeId);
            }
            boolean success = allNodes.add(nodeId);
            if (success) {
                newNodeQueue.add(NodeEvent.buildAdd(nodeId));
            }
            return success;
        }
    }

    public static boolean removeNode(String nodeId){
        synchronized (allNodes) {
            //是否为raft服务端
            if (selfId != null) {
                otherNodes.remove(nodeId);
            }
            boolean success = allNodes.remove(nodeId);
            if (success) {
                newNodeQueue.add(NodeEvent.buildRemove(nodeId));
            }
            return success;
        }
    }

}
