package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 基准测试：演示 Semaphore 的限流与资源控制
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)         // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SemaphoreExample {

    private Semaphore semaphore;

    @Setup(Level.Iteration)
    public void setup() {
        // 初始化一个有 3 个许可的信号量
        semaphore = new Semaphore(3);
    }

    /**
     * 基准方法：模拟线程获取和释放信号量
     */
    @Benchmark
    @Fork(1)                     // 启动一个独立的 JVM
    @Warmup(iterations = 2)       // 预热 2 次
    @Measurement(iterations = 3)  // 实测 3 次
    public void testSemaphore(Blackhole bh) {
        try {
            semaphore.acquire();   // 获取一个许可
            // 模拟临界区操作
            bh.consume("临界区操作 " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();   // 释放许可
        }
    }
}
