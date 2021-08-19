package code.distribution.raft.kv;

/**
 * 〈Kv操作类型〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-04
 */
public interface KvOpType {

    int GET = 0;

    int SET = 1;

    int DEL = 2;
}
