package code.distribution.raft.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import static code.distribution.raft.enums.NodeEventType.ADD;
import static code.distribution.raft.enums.NodeEventType.REMOVE;

/**
 * 〈节点事件〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/09
 */
@Data
@AllArgsConstructor
public class NodeEvent {

    protected String nodeId;

    protected int eventType;

    public static NodeEvent buildAdd(String nodeId){
        return new NodeEvent(nodeId, ADD);
    }

    public static NodeEvent buildRemove(String nodeId){
        return new NodeEvent(nodeId, REMOVE);
    }

    public String toString(){
        return eventType + ":" + nodeId;
    }
}
