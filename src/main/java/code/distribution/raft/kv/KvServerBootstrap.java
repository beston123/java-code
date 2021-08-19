package code.distribution.raft.kv;

import code.distribution.raft.boot.RaftBootstrap;

/**
 * 〈Kv服务端启动〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
public class KvServerBootstrap {

    /**
     * java code.distribution.raft.boot.KvServerBootstrap 127.0.0.1:2001 127.0.0.1:2001,127.0.0.1:2002,127.0.0.1:2003
     *
     * @param args
     */
    public static void main(String[] args) {
        new RaftBootstrap().start(args, new KvStateMachine());
    }

}
