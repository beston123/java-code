package code.distribution.raft.log;

import code.distribution.raft.RaftConst;
import code.distribution.raft.model.LogEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 〈日志模块〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2019-11-05
 */
public class LogModule {

    /**
     * 日志条目集；
     * 每一个条目包含一个用户状态机执行的指令，和收到时的任期号
     */
    private List<LogEntry> logs = new CopyOnWriteArrayList();

    private transient ReentrantReadWriteLock logLock = new ReentrantReadWriteLock();

    public LogEntry indexOf(int logIndex) {
        logLock.readLock().lock();
        try {
            if (logs.size() > logIndex) {
                return logs.get(logIndex);
            } else {
                return null;
            }
        } finally {
            logLock.readLock().unlock();
        }
    }

    /**
     * 从fromIndex开始删除后面（包括fromIndex）的所有日志
     * @param fromIndex
     */
    public void removeFrom(int fromIndex) {
        logLock.writeLock().lock();
        try {
            if (logs.size() > fromIndex) {
                Iterator<LogEntry> iterator = logs.iterator();
                int startIndex = 0;
                while (iterator.hasNext()) {
                    iterator.next();
                    if (startIndex >= fromIndex) {
                        iterator.remove();
                    }
                    startIndex++;
                }
            }
        } finally {
            logLock.writeLock().unlock();
        }
    }

    /**
     * 在index位置追加日志
     * @param index
     * @param entry
     */
    public void append(int index, LogEntry entry) {
        logLock.writeLock().lock();
        try {
            logs.add(index, entry);
        } finally {
            logLock.writeLock().unlock();
        }
    }

    /**
     * 追加日志
     * @param entry
     */
    public void append(LogEntry entry) {
        logLock.writeLock().lock();
        try {
            logs.add(entry);
        } finally {
            logLock.writeLock().unlock();
        }
    }

    /**
     * 最后一条日志的index和日志项
     * @return
     */
    public Pair<Integer, LogEntry> lastLog() {
        logLock.readLock().lock();
        try {
            if (logs.size() == 0) {
                return null;
            } else {
                int lastIndex = logs.size() - 1;
                return Pair.of(lastIndex, logs.get(lastIndex));
            }
        } finally {
            logLock.readLock().unlock();
        }
    }

    /**
     * 最后一条日志的index
     * @return
     */
    public int lastLogIndex() {
        logLock.readLock().lock();
        try {
            return logs.size() - 1;
        } finally {
            logLock.readLock().unlock();
        }
    }

    /**
     * 截取日志
     *
     * @param fromIndex 包括该位置
     * @param endIndex  包括该位置
     * @return
     */
    public List<LogEntry> subLogs(int fromIndex, int endIndex) {
        logLock.readLock().lock();
        try {
            return logs.subList(fromIndex, endIndex + 1);
        } finally {
            logLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Index").append("\t");
        for (int i = 0; i < logs.size(); i++) {
            stringBuilder.append(i).append("\t");
        }
        stringBuilder.append(RaftConst.LINE_SEP);

        stringBuilder.append("Term").append("\t");
        for (int i = 0; i < logs.size(); i++) {
            stringBuilder.append(logs.get(i).getTerm()).append("\t");
        }
        stringBuilder.append(RaftConst.LINE_SEP);

        stringBuilder.append("Value").append("\t");
        for (int i = 0; i < logs.size(); i++) {
            stringBuilder.append(logs.get(i).getCommand()).append("\t");
        }
        stringBuilder.append(RaftConst.LINE_SEP);
        return stringBuilder.toString();
    }
}
