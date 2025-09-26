package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * 基准测试：演示 ThreadLocal 的 set/get 性能
 */
@State(Scope.Thread)  // 每个线程独立维护状态，适合 ThreadLocal
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ThreadLocalExample {

    private ThreadLocal<Integer> threadLocal;

    @Setup(Level.Iteration)
    public void setup() {
        threadLocal = ThreadLocal.withInitial(() -> 0);
    }

    /**
     * 基准方法：测试 set
     */
    @Benchmark
    @Fork(1)                     // 启动一个 JVM
    @Warmup(iterations = 2)       // 预热 2 次
    @Measurement(iterations = 3)  // 实测 3 次
    public void testSet(Blackhole bh) {
        int value = threadLocal.get() + 1;
        threadLocal.set(value);
        bh.consume(value);
    }

    /**
     * 基准方法：测试 get
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testGet(Blackhole bh) {
        int value = threadLocal.get();
        bh.consume(value);
    }
}
