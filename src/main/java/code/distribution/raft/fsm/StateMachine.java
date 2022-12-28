package code.distribution.raft.fsm;

import code.distribution.raft.model.Command;
import code.distribution.raft.model.LogEntry;
import code.distribution.raft.model.NodeCommand;

/**
 * 〈状态机〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-04
 */
public interface StateMachine {

    default void apply(LogEntry logEntry) {
        if (logEntry.getCommand() instanceof NodeCommand) {
            NodeStateMachine.getInstance().apply(logEntry.getCommand());
        } else {
            apply(logEntry.getCommand());
        }
    }

    void apply(Command command);

    Object get(String key);

    void set(String key, Object value);

    void del(String key);

}
