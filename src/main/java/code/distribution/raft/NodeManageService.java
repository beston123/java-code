package code.distribution.raft;

import code.distribution.raft.enums.NodeEventType;
import code.distribution.raft.enums.RoleType;
import code.distribution.raft.model.NodeEvent;
import code.util.NamedThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈节点管理服务〉<p>
 * 〈功能详细描述〉
 *
 * @author beston
 * @date 2020/12/08
 */
public class NodeManageService implements IService {

    private final RaftNodeServer nodeServer;

    private boolean stop;

    public NodeManageService(RaftNodeServer nodeServer) {
        this.nodeServer = nodeServer;
    }

    private final ScheduledExecutorService nodeManagerTimer = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("NodeManagerTimer-"), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void start() {
        stop = false;
        nodeManagerTimer.scheduleAtFixedRate(() -> {
            while (!stop) {
                if (nodeServer.getNode().getRole() == RoleType.LEADER) {
                    try {
                        NodeEvent nodeEvent = RaftClusterManager.newNodeQueue().take();
                        System.out.println("Handle node event:" + nodeEvent);
                        if (nodeEvent.getEventType() == NodeEventType.ADD) {
                            nodeServer.addNode(nodeEvent.getNodeId());
                        } else if (nodeEvent.getEventType() == NodeEventType.REMOVE) {
                            nodeServer.removeNode(nodeEvent.getNodeId());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, RaftConst.SCHEDULER_DELAY_MS, RaftConst.SLEEP_DEVIATION_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        stop = true;
        nodeManagerTimer.shutdownNow();
    }

}
