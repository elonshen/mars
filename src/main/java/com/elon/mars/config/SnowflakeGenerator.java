package com.elon.mars.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

/**
 * 雪花ID生成器
 * 每秒支持生成4096个不重复的ID
 * 可以使用到2093年
 */
@Component
public class SnowflakeGenerator implements IdentifierGenerator {

    // 起始时间戳 - 2024-01-01
    private static final long START_TIMESTAMP = 1704038400000L;
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;
    // 序列号最大值 4095
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 处理时钟回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟回拨,拒绝生成ID");
        }

        // 如果是同一毫秒生成的, 则进行序列自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 同一毫秒序列号用完了,等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不是同一毫秒,序列号重置为0
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 时间戳左移12位 | 序列号
        return ((timestamp - START_TIMESTAMP) << SEQUENCE_BITS) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {

        if (object == null) {
            return nextId();
        }

        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField("id");
            field.setAccessible(true);
            Object id = field.get(object);

            // 修改判断条件
            if (id instanceof Long && (Long) id > 0) {
                return id;
            }
        } catch (Exception ignored) {
            // 获取失败则生成新ID
            throw new RuntimeException("获取ID失败,ID字段必须为Long类型");
        }

        return nextId();
    }
}