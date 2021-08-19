package code.distribution.raft.kv;

import code.distribution.raft.RaftConfig;
import code.distribution.raft.client.ClientReq;
import code.distribution.raft.client.ClientRet;
import code.distribution.raft.client.RaftClient;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈Kv客户端〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-06
 */

public class KvClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KvClient.class);

    private RaftClient raftClient;

    @Before
    public void before(){
        RaftConfig raftConfig = new RaftConfig();
        raftConfig.setClusterNodes("127.0.0.1:2001,127.0.0.1:2002,127.0.0.1:2003");
        raftClient = new RaftClient(raftConfig);
        raftClient.refresh();
    }

    @Test
    public void set(){
        set("a", "1");
    }

    private void set(String key, String value){
        KvCommand kvCommand = KvCommand.buildSet(key, value);
        ClientReq clientReq = new ClientReq(false, kvCommand);

        ClientRet clientRet = raftClient.invoke(clientReq);
        if(clientRet != null && clientRet.isSuccess()){
            LOGGER.info("Set success, {}=>{}", key, value);
        }
    }


    @Test
    public void get(){
        get("a");
    }

    private void get(String key){
        KvCommand kvCommand = KvCommand.buildGet(key);
        ClientReq clientReq = new ClientReq(true, kvCommand);

        ClientRet clientRet = raftClient.invoke(clientReq);
        if(clientRet != null && clientRet.isSuccess()){
            LOGGER.info("Get success, {}=>{}", key, clientRet.getValue());
        }
    }

    @Test
    public void del(){
        del("a");
    }

    private void del(String key){
        KvCommand kvCommand = KvCommand.buildDel(key);
        ClientReq clientReq = new ClientReq(false, kvCommand);

        ClientRet clientRet = raftClient.invoke(clientReq);
        if(clientRet != null && clientRet.isSuccess()){
            LOGGER.info("Del success, key={}", key);
        }
    }

    @Test
    public void crud(){
        set("a", "1");
        set("b", "2");
        set("c", "3");

        System.out.println("------------------");

        get("a");// a=>1
        get("b");// b=>2
        get("c");// c=>3
        get("d");// d=>null

        System.out.println("------------------");

        set("a", "4");
        del("b");
        set("d", "4");

        System.out.println("------------------");

        get("a");// a=>4
        get("b");// b=>null
        get("c");// c=>3
        get("d");// d=>4
        System.out.println("------------------");
    }

    @Test
    public void crud2(){
        set("b", "8");
        set("c", "9");
        del("d");

        System.out.println("------------------");

        get("a");// a=>4
        get("b");// b=>8
        get("c");// c=>9
        get("d");// d=>null

        System.out.println("------------------");
    }

    @Test
    public void test() throws InterruptedException {
        KvCommand kvCommand = KvCommand.buildGet("a");
        ClientReq clientReq = new ClientReq(true, kvCommand);
        String nodeId = raftClient.getLeader();
        while (true){
            ClientRet clientRet = raftClient.invoke(nodeId, clientReq);
            if(clientRet == null){
                nodeId = raftClient.randomNode();
                LOGGER.info("Pick a random node {}", nodeId);
                Thread.sleep(1000);
            }else if(clientRet.isSuccess()){
                LOGGER.info("Get success, {}=>{}", kvCommand.getKey(), clientRet.getValue());
                Thread.sleep(3000);
            }else if(clientRet.getLeaderId() != null){
                nodeId = clientRet.getLeaderId();
                LOGGER.info("Switch to new leader {}", nodeId);
            }
        }
    }

}
