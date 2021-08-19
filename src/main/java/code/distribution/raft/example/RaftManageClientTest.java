package code.distribution.raft.example;

import code.distribution.raft.RaftConfig;
import code.distribution.raft.client.RaftManageClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/08
 */
@Slf4j
public class RaftManageClientTest {

    private RaftManageClient raftManager;

    @Before
    public void before(){
        RaftConfig raftConfig = new RaftConfig();
        raftConfig.setClusterNodes("127.0.0.1:2001,127.0.0.1:2002,127.0.0.1:2003");
        raftManager = new RaftManageClient(raftConfig);
        raftManager.refresh();
    }

    @Test
    public void addNode(){
        raftManager.addNode("127.0.0.1:2004");
    }

    @Test
    public void removeNode(){
        raftManager.removeNode("127.0.0.1:2002");
    }

}
