package com.tutict.jucdemospringboot.exam.pool;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 对比 WorkStealingPool 和 VirtualThread 在计算任务上的表现
 */
@State(Scope.Benchmark)
public class WorkStealingPoolBenchmark {

    private ExecutorService workStealingPool;
    private ExecutorService virtualThreadPool;

    @Setup(Level.Iteration)
    public void setup() {
        // ForkJoinPool.commonPool() 作为 WorkStealingPool
        workStealingPool = Executors.newWorkStealingPool();
        // 虚拟线程池
        virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        workStealingPool.shutdown();
        virtualThreadPool.shutdown();
        workStealingPool.awaitTermination(1, TimeUnit.SECONDS);
        virtualThreadPool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 运行 20 个计算型任务
     */
    private void runTasks(ExecutorService executor, Blackhole bh) {
        List<Callable<Long>> tasks = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> (Callable<Long>) () -> {
                    // 计算斐波那契数列，模拟 CPU 密集任务
                    long result = fibonacci(30);
                    // 吃掉结果避免被 JIT 优化掉
                    bh.consume(result);
                    return result;
                })
                .toList();

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Benchmark
    @Fork(1)                // 启动一个独立的 JVM 进程运行测试
    @Warmup(iterations = 2) // 预热2次
    @Measurement(iterations = 3)
    public void testWorkStealingPool(Blackhole bh) {
        runTasks(workStealingPool, bh);
    }

    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVirtualThreadPool(Blackhole bh) {
        runTasks(virtualThreadPool, bh);
    }

    // 简单递归 Fibonacci，用于模拟计算密集型任务
    private static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
