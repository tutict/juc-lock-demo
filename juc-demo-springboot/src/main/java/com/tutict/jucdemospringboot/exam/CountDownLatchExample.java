package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 基准测试：演示 CountDownLatch 在并发任务等待中的性能
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)      // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CountDownLatchExample {

    private ExecutorService executor;

    @Setup(Level.Iteration)
    public void setup() {
        executor = Executors.newFixedThreadPool(8); // 固定线程池
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 使用 CountDownLatch 等待多个任务完成
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testCountDownLatch(Blackhole bh) {
        int taskCount = 50;
        CountDownLatch latch = new CountDownLatch(taskCount);

        IntStream.range(0, taskCount).forEach(i -> executor.submit(() -> {
            try {
                // 模拟任务执行
                TimeUnit.MILLISECONDS.sleep(10);
                bh.consume(i * i); // 假装有计算结果
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown(); // 任务完成，计数减一
            }
        }));

        try {
            latch.await(); // 等待所有任务完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
