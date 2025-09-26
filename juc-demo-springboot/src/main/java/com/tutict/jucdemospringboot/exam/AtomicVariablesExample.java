package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基准测试：对比 AtomicInteger 与 synchronized int 的性能
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)   // 吞吐量模式：每秒执行的操作数
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AtomicVariablesExample {

    private AtomicInteger atomicCounter;
    private int normalCounter;

    private final Object lock = new Object();

    @Setup(Level.Iteration)
    public void setup() {
        atomicCounter = new AtomicInteger(0);
        normalCounter = 0;
    }

    /**
     * 使用 AtomicInteger.incrementAndGet()
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testAtomicIncrement(Blackhole bh) {
        int value = atomicCounter.incrementAndGet();
        bh.consume(value);
    }

    /**
     * 使用 synchronized 锁保护普通 int
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testSynchronizedIncrement(Blackhole bh) {
        int value;
        synchronized (lock) {
            normalCounter++;
            value = normalCounter;
        }
        bh.consume(value);
    }
}
