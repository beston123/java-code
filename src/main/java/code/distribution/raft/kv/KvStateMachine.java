package code.distribution.raft.kv;

import code.distribution.raft.RaftConst;
import code.distribution.raft.fsm.StateMachine;
import code.distribution.raft.model.Command;
import code.distribution.raft.model.LogEntry;

import java.util.HashMap;
import java.util.Map;

import static code.distribution.raft.kv.KvOpType.DEL;
import static code.distribution.raft.kv.KvOpType.SET;

/**
 * 〈Kv状态机〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-04
 */
public class KvStateMachine implements StateMachine {

    private Map<String, String> kvMap = new HashMap<>();

    @Override
    public void apply(Command command) {
        KvCommand kvCommand = (KvCommand) command;

        if (SET == kvCommand.getOpType()) {
            set(kvCommand.getKey(), kvCommand.getValue());
        } else if (DEL == kvCommand.getOpType()) {
            del(kvCommand.getKey());
        }
    }

    @Override
    public String get(String key) {
        return kvMap.get(key);
    }

    @Override
    public void set(String key, Object value) {
        kvMap.put(key, (String)value);
    }

    @Override
    public void del(String key) {
        kvMap.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=>").append(entry.getValue()).append(RaftConst.LINE_SEP);
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        StateMachine stateMachine = new KvStateMachine();
        stateMachine.set("a", "1");
        stateMachine.set("b", "2");
    }

}
