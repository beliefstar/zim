package org.zim.common;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * single machine snowflake algorithm
 */
public class SnowFlakeGenerator {

    private static final long start_milli = 1577808000000L;
    private static final int max_seq = (1 << 15) - 1;
    private static final int milli_offset = 15;

    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile long last_milli = currentMilli();

    public synchronized long nextId() {
        long millis = currentMilli();
        long seq = 0;
        if (millis == last_milli) {
            seq = sequence.incrementAndGet() & max_seq;
            if (seq == 0) {
                millis = nextMilli();
            }
        } else {
            sequence.set(0);
        }
        last_milli = millis;

        return (millis - start_milli) << milli_offset | seq;
    }

    private long currentMilli() {
        return Instant.now().toEpochMilli();
    }

    private long nextMilli() {
        long milli = currentMilli();
        while (milli <= last_milli) {
            milli = currentMilli();
        }
        return milli;
    }
}
