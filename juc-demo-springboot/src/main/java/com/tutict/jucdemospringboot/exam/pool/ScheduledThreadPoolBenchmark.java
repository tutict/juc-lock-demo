package com.tutict.jucdemospringboot.exam.pool;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * 对比 ScheduledThreadPool 和 VirtualThread 执行周期性任务的性能
 */
@State(Scope.Benchmark)
public class ScheduledThreadPoolBenchmark {

    private static final Logger logger = Logger.getGlobal();

    private ScheduledExecutorService scheduledThreadPool;
    private ExecutorService virtualThreadPool;

    @Setup(Level.Iteration)
    public void setup() {
        // 固定大小的定时线程池
        scheduledThreadPool = Executors.newScheduledThreadPool(4);
        // 虚拟线程池（每个任务一个虚拟线程）
        virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        scheduledThreadPool.shutdown();
        virtualThreadPool.shutdown();
        scheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        virtualThreadPool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 基准任务：调度 1000 个延迟任务
     */
    private void scheduleTasks(ScheduledExecutorService executor) {
        CountDownLatch latch = new CountDownLatch(1000);
        IntStream.range(0, 1000).forEach(i -> {
            executor.schedule(() -> {
                latch.countDown(); // 模拟轻量任务
            }, 10, TimeUnit.MILLISECONDS);
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void runVirtualTasks(ExecutorService executor) {
        CountDownLatch latch = new CountDownLatch(1000);
        IntStream.range(0, 1000).forEach(i -> {
            executor.execute(() -> {
                try {
                    Thread.sleep(10); // 模拟延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testScheduledThreadPool() {
        scheduleTasks(scheduledThreadPool);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVirtualThreadPoolAsScheduler() {
        runVirtualTasks(virtualThreadPool);
    }
}
