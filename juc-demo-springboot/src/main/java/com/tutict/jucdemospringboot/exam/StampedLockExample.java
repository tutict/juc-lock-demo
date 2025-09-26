package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * 基准测试：演示 StampedLock 的读写性能
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)          // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StampedLockExample {

    private final StampedLock stampedLock = new StampedLock();
    private int sharedData = 0;

    /**
     * 写操作：独占写锁
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testWriteLock(Blackhole bh) {
        long stamp = stampedLock.writeLock();
        try {
            sharedData++;
            bh.consume(sharedData);
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    /**
     * 读操作：乐观读锁 + 回退策略
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testOptimisticRead(Blackhole bh) {
        long stamp = stampedLock.tryOptimisticRead();
        int value = sharedData;

        // 校验期间是否有写操作
        if (!stampedLock.validate(stamp)) {
            // 回退到悲观读锁
            stamp = stampedLock.readLock();
            try {
                value = sharedData;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        bh.consume(value);
    }
}
