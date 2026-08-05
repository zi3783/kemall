package com.kemall.common.utils;

/**
 * 极简雪花算法ID生成器
 *
 * @author your-name
 */
public class SnowflakeGenerator {

    // ===== 固定参数 =====
    private static final long START_TIME = 1609459200000L; // 2021-01-01
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 31
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS); // 4095

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // ===== 实例变量 =====
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param workerId 机器ID（0-31）
     */
    public SnowflakeGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 必须在 0-31 之间");
        }
        this.workerId = workerId;
    }

    /**
     * 生成唯一ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIME) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }
}