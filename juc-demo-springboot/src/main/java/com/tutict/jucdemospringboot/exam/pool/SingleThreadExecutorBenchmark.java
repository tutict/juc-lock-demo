package com.tutict.jucdemospringboot.exam.pool;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * 对比 SingleThreadExecutor 和 VirtualThread 执行 1000 个任务的性能
 */
@State(Scope.Benchmark)
public class SingleThreadExecutorBenchmark {

    private static final Logger logger = Logger.getGlobal();

    private ExecutorService singleThreadExecutor;
    private ExecutorService virtualThreadExecutor;

    @Setup(Level.Iteration)
    public void setup() {
        // 单线程执行器（所有任务顺序执行）
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        // 虚拟线程池（每个任务一个线程，天然并发）
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        singleThreadExecutor.shutdown();
        virtualThreadExecutor.shutdown();
        singleThreadExecutor.awaitTermination(1, TimeUnit.SECONDS);
        virtualThreadExecutor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 模拟执行 1000 个小任务
     */
    private void runTasks(ExecutorService executor) {
        List<Callable<String>> tasks = IntStream.range(0, 1000)
                .mapToObj(i -> (Callable<String>) () -> {
                    // 模拟轻量计算任务
                    int sum = 0;
                    for (int j = 0; j < 1000; j++) {
                        sum += j;
                    }
                    return "task-" + i + "-" + sum;
                })
                .toList();

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testSingleThreadExecutor() {
        runTasks(singleThreadExecutor);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVirtualThreadExecutor() {
        runTasks(virtualThreadExecutor);
    }
}
