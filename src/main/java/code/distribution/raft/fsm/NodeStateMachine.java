package code.distribution.raft.fsm;

import code.distribution.raft.RaftClusterManager;
import code.distribution.raft.model.Command;
import code.distribution.raft.model.NodeCommand;
import lombok.extern.slf4j.Slf4j;

import static code.distribution.raft.enums.NodeEventType.ADD;
import static code.distribution.raft.enums.NodeEventType.REMOVE;

/**
 * 〈节点状态机〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/08
 */
@Slf4j
public class NodeStateMachine implements StateMachine {

    private static StateMachine instance = new NodeStateMachine();

    public static StateMachine getInstance() {
        return instance;
    }

    @Override
    public void apply(Command command) {
        NodeCommand cmd = (NodeCommand) command;
        if (cmd.getEventType() == ADD) {
            set(cmd.getNodeId(), null);
        } else if (cmd.getEventType() == REMOVE) {
            del(cmd.getNodeId());
        }
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void set(String key, Object value) {
        log.info("Node {} add into cluster.", key);
        RaftClusterManager.addNode(key);
    }

    @Override
    public void del(String key) {
        log.info("Node {} remove from cluster.", key);
        RaftClusterManager.removeNode(key);
    }
}
