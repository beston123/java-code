package code.distribution.raft.boot;

import code.distribution.raft.RaftConfig;
import code.distribution.raft.RaftNodeServer;
import code.distribution.raft.fsm.StateMachine;

/**
 * 〈Raft启动端〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
public class RaftBootstrap {

    /**
     * java [bootClass] 127.0.0.1:2001 127.0.0.1:2001,127.0.0.1:2002,127.0.0.1:2003
     *
     * @param args
     */
    public void start(String[] args, StateMachine stateMachine){
        if(args.length != 2){
            System.out.println("Usage: ");
            System.out.println("\tjava [bootstrapClass] [nodeId] [clusterNodes...]");
            System.out.println("Example: java org.raft.BootStrap 127.0.0.1:2001 127.0.0.1:2001,127.0.0.1:2002,127.0.0.1:2003");
            System.exit(1);
        }
        RaftConfig raftConfig = new RaftConfig();
        raftConfig.setNodeId(args[0]);
        raftConfig.setClusterNodes(args[1]);
        start(raftConfig, stateMachine);
    }

    public void start(RaftConfig raftConfig, StateMachine stateMachine){
        RaftNodeServer raftNodeServer = new RaftNodeServer(raftConfig.getNodeId(), stateMachine);
        raftNodeServer.initConfig(raftConfig);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                raftNodeServer.close();
            }
        }));
        raftNodeServer.start();
    }

}
