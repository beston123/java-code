package code.distribution.raft.util;

import code.distribution.raft.RaftConst;
import code.distribution.raft.RaftNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-06
 */
public class SnapshotUtils {

    public static void save(RaftNode raftNode){
        try {
            String raftHome = System.getProperty("user.home") + "/raft/";
            File raftHomeFile = new File(raftHome);
            if (!raftHomeFile.exists()) {
                raftHomeFile.mkdir();
            } else if (!raftHomeFile.isDirectory()) {
                throw new IOException("Raft home is not directory: " + raftHome);
            }
            File file = new File(raftHome + raftNode.getNodeId().replace(":", "_"));
            file.createNewFile();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("--------------------").append(LocalDateTime.now()).append("--------------------").append(RaftConst.LINE_SEP);
            stringBuilder.append("Summary:").append(RaftConst.LINE_SEP);
            stringBuilder.append(getSummary(raftNode)).append(RaftConst.LINE_SEP);

            stringBuilder.append("LogEntry:").append(RaftConst.LINE_SEP);
            stringBuilder.append(raftNode.getLogModule().toString()).append(RaftConst.LINE_SEP);

            stringBuilder.append("StateMachine:").append(RaftConst.LINE_SEP);
            stringBuilder.append(raftNode.getStateMachine().toString());

            System.out.println(stringBuilder.toString());

            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
            fileWriter.write(stringBuilder.toString());
            fileWriter.newLine();

            fileWriter.flush();
            fileWriter.close();

            System.out.println("Save snapshot success, "+ file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getSummary(RaftNode raftNode){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("nodeId: ").append(raftNode.getNodeId()).append(RaftConst.LINE_SEP)
                .append("roleType: ").append(raftNode.getRole()).append(RaftConst.LINE_SEP)
                .append("currentTerm: ").append(raftNode.getCurrentTerm().get()).append(RaftConst.LINE_SEP)
                .append("commitIndex: ").append(raftNode.getCommitIndex().get()).append(RaftConst.LINE_SEP)
                .append("lastApplied: ").append(raftNode.getLastApplied().get()).append(RaftConst.LINE_SEP);
        return stringBuilder.toString();
    }
}
