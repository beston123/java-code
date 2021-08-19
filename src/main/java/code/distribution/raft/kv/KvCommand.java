package code.distribution.raft.kv;

import code.distribution.raft.model.BaseCommand;
import lombok.Data;

/**
 * 〈Kv命令〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-04
 */
@Data
public class KvCommand extends BaseCommand {

    private Integer opType;

    private String value;

    public KvCommand() {
    }

    public KvCommand(Integer opType, String key, String value) {
        this.key = key;
        this.opType = opType;
        this.value = value;
    }

    public static KvCommand buildGet(String key){
        return new KvCommand(KvOpType.GET, key, null);
    }

    public static KvCommand buildSet(String key, String value){
        return new KvCommand(KvOpType.SET, key, value);
    }

    public static KvCommand buildDel(String key){
        return new KvCommand(KvOpType.DEL, key, null);
    }

    public String toString(){
        return opType + ":" + key + "=>" +value;
    }
}
