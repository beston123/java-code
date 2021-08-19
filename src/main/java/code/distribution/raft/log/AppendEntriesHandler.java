package code.distribution.raft.log;

import code.distribution.raft.IHandler;
import code.distribution.raft.RaftNode;
import code.distribution.raft.RaftNodeServer;
import code.distribution.raft.model.AppendEntriesReq;
import code.distribution.raft.model.AppendEntriesRet;
import code.distribution.raft.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈附加日志 处理器〉<p>
 * 接收者实现：
 * <p>
 * 1、如果 term < currentTerm 就返回 false （5.1 节）
 * 2、如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false
 * 3、如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的
 * 4、附加日志中尚未存在的任何新条目
 * 5、如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和新日志条目索引值 中较小的一个
 *
 * @author zixiao
 * @date 2019/3/11
 */
public class AppendEntriesHandler implements IHandler<AppendEntriesReq, AppendEntriesRet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppendEntriesHandler.class);

    private final RaftNode node;

    private final RaftNodeServer nodeServer;

    public AppendEntriesHandler(RaftNode node, RaftNodeServer nodeServer) {
        this.node = node;
        this.nodeServer = nodeServer;
    }

    @Override
    public AppendEntriesRet handle(AppendEntriesReq appendEntriesReq) {
        int currentTerm = node.currentTerm();
        //1.1如果收到的任期比当前任期小,返回false
        if (appendEntriesReq.getTerm() < currentTerm) {
            return AppendEntriesRet.fail(currentTerm);
        //1.2如果RPC请求或者响应包含的任期T > currentTerm，将currentTerm设置为T，并转换为Follower
        } else if (appendEntriesReq.getTerm() > currentTerm) {
            nodeServer.changeToFollower(currentTerm, appendEntriesReq.getTerm());
            currentTerm = appendEntriesReq.getTerm();
        } else {
            nodeServer.changeToFollower(currentTerm, appendEntriesReq.getTerm());
        }

        //2、接受Leader节点的AppendEntries请求
        nodeServer.acceptLeaderRpc(appendEntriesReq.getLeaderId());

        //3、处理日志复制
        if (appendEntriesReq.getEntries() == null) {
            return heartbeat(appendEntriesReq, currentTerm);
        }
        return appendEntries(appendEntriesReq, currentTerm);
    }

    /**
     * 处理心跳
     *
     * @param appendEntriesReq
     * @param currentTerm
     * @return
     */
    private AppendEntriesRet heartbeat(AppendEntriesReq appendEntriesReq, int currentTerm) {
        LOGGER.debug("Get heartbeat from leader {}", appendEntriesReq.getLeaderId());
        // 提交（leader已经提交的）日志
        commitLog(appendEntriesReq.getLeaderCommit());
        return AppendEntriesRet.success(currentTerm);
    }

    /**
     * 附加日志，接收者实现：
     * 1、如果收到的任期比当前任期小,返回false
     * 2、如果不包含之前的日志条目（没有匹配 prevLogIndex 和 prevLogTerm）,返回false
     * 3、如果存在index相同，但是term不相同的日志，删除从该位置开始所有的日志
     * 4、追加所有不存在的日志
     * 5、如果leaderCommit>commitIndex，将commitIndex设置为commitIndex = min(leaderCommit, index of last new entry)
     *
     * @param appendEntriesReq
     * @param currentTerm
     * @return
     */
    private AppendEntriesRet appendEntries(AppendEntriesReq appendEntriesReq, int currentTerm) {
        LOGGER.info("Get append entries request from leader {}, leaderCommit: {}", appendEntriesReq.getLeaderId(), appendEntriesReq.getLeaderCommit());
        //2、如果不包含之前的日志条目（没有匹配 prevLogIndex 和 prevLogTerm）,返回false
        if (appendEntriesReq.getPrevLogIndex() >= 0) {
            LogEntry prevLog = node.getLogModule().indexOf(appendEntriesReq.getPrevLogIndex());
            // prevLogIndex位置的日志不存在或term与prevLogTerm不相等
            if (prevLog == null || prevLog.getTerm() != appendEntriesReq.getPrevLogTerm()) {
                return AppendEntriesRet.fail(currentTerm);
            }
        } else if (appendEntriesReq.getPrevLogIndex() == -1) {
            //第一条日志（leader复制从位置0开始的日志），没有preLog
        }

        LogEntry[] entries = appendEntriesReq.getEntries();
        int idx = appendEntriesReq.getPrevLogIndex();
        for (LogEntry entry : entries) {
            idx++;
            LogEntry existedLog = node.getLogModule().indexOf(idx);
            if (existedLog != null) {
                //3、如果存在index相同，但是term不相同的日志，删除从该位置开始所有的日志
                if (entry.getTerm() != existedLog.getTerm()) {
                    node.getLogModule().removeFrom(idx);
                    node.getLogModule().append(entry);
                } else {
                    //已存在相同日志，不处理
                }
            } else {
                //4、追加所有不存在的日志
                node.getLogModule().append(entry);
            }
        }

        //5、提交（leader已经提交的）日志
        commitLog(appendEntriesReq.getLeaderCommit());

        return AppendEntriesRet.success(currentTerm);
    }

    /**
     * 提交日志
     * 如果leaderCommit > commitIndex，将commitIndex设置为commitIndex = min(leaderCommit, index of last new entry)
     *
     * @param leaderCommit
     */
    private void commitLog(int leaderCommit) {
        int commitIndex = node.getCommitIndex().get();
        if (leaderCommit > commitIndex) {
            int lastIndex = node.getLogModule().lastLogIndex();
            if (lastIndex < 0) {
                //新加入的节点 还没有接收过日志
                return;
            }
            LOGGER.info("Commit log, leaderCommit:{}, commitIndex:{}, lastIndex:" + lastIndex, leaderCommit, commitIndex);

            int newCommitIndex = Math.min(leaderCommit, lastIndex);
            node.setCommitIndex(newCommitIndex);
            //应用状态机到 commitIndex
            node.applyTo(newCommitIndex);
        }
    }

}
