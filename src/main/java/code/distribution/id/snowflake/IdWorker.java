package code.distribution.id.snowflake;

/**
 * 〈Snowflake算法〉<p>
 * 〈功能详细描述〉
 * @see https://github.com/seata/seata/blob/1.4.0/common/src/main/java/io/seata/common/util/IdWorker.java
 * @author tianwu
 * @date 2021/06/16
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

public class IdWorker {

    private volatile static IdWorker idWorker = null;

    /**
     * Start time cut (2020-05-03)
     */
    private final long twepoch = 1588435200000L;

    /**
     * The number of bits occupied by the machine id
     */
    private final long workerIdBits = 10L;

    /**
     * Maximum supported machine id, the result is 1023 (this shift algorithm can quickly calculate the largest decimal
     * number that can be represented by a few binary numbers)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * The number of bits the sequence occupies in id
     */
    private final long sequenceBits = 12L;

    /**
     * Machine ID left 12 digits
     */
    private final long workerIdShift = sequenceBits;

    /**
     * Time truncated to the left by 22 bits (10 + 12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits;

    /**
     * Generate sequence mask
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * Machine ID (0 ~ 1023)
     */
    private long workerId;

    /**
     * Sequence in milliseconds (0 ~ 4095)
     */
    private long sequence = 0L;

    /**
     * Time of last ID generation
     */
    private long lastTimestamp = -1L;

    /**
     * Constructor
     *
     * @param workerId
     *            Job ID (0 ~ 1023)
     */
    public IdWorker(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    /**
     * Get the next ID (the method is thread-safe)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = ThreadLocalRandom.current().nextLong(0, 256);
        }
        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * Block until the next millisecond until a new timestamp is obtained
     *
     * @param lastTimestamp
     *            Time of last ID generation
     * @return Current timestamp
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * Returns the current time in milliseconds
     *
     * @return Current time (ms)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public static IdWorker getInstance() {
        if (idWorker == null) {
            synchronized (IdWorker.class) {
                if (idWorker == null) {
                    init(initWorkerId());
                }
            }
        }
        return idWorker;
    }

    private static long initWorkerId() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!",e);
        }
        byte[] ipAddressByteArray = address.getAddress();
        return ((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE) + (ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF);
    }

    private static void init(Long serverNodeId) {
        if (idWorker == null) {
            synchronized (IdWorker.class) {
                if (idWorker == null) {
                    idWorker = new IdWorker(serverNodeId);
                }
            }
        }
    }

}