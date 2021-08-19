package code.distribution.raft.model;

import static code.distribution.raft.enums.NodeEventType.ADD;
import static code.distribution.raft.enums.NodeEventType.REMOVE;

/**
 * 〈节点变更命令〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/08
 */
public class NodeCommand extends NodeEvent implements Command {

    public NodeCommand(String nodeId, int opType) {
        super(nodeId, opType);
    }

    public static NodeCommand buildAdd(String nodeId){
        return new NodeCommand(nodeId, ADD);
    }

    public static NodeCommand buildRemove(String nodeId){
        return new NodeCommand(nodeId, REMOVE);
    }

    public String toString(){
        return eventType + ":" + nodeId;
    }
}
