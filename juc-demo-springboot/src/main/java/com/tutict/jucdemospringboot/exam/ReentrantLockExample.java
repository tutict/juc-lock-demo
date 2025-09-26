package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基准测试：演示 ReentrantLock 的加锁/解锁性能
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)         // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ReentrantLockExample {

    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;

    /**
     * 基准方法：多个线程竞争 ReentrantLock
     */
    @Benchmark
    @Fork(1)                     // 启动一个 JVM 进程
    @Warmup(iterations = 2)       // 预热 2 次
    @Measurement(iterations = 3)  // 实测 3 次
    public void testReentrantLock(Blackhole bh) {
        lock.lock();
        try {
            counter++;
            bh.consume(counter); // 防止 JIT 优化
        } finally {
            lock.unlock();
        }
    }
}
