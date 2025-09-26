package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 基准测试：演示 ExecutorService 提交任务并获取结果
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)       // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ExecutorServiceExample {

    private ExecutorService executor;

    @Setup(Level.Iteration)
    public void setup() {
        // 固定线程池，模拟 CPU 密集型任务
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 提交一批任务到 ExecutorService 并等待结果
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testExecutorService(Blackhole bh) {
        int taskCount = 20;
        List<Future<Integer>> futures = new ArrayList<>();

        // 提交任务
        IntStream.range(0, taskCount).forEach(i ->
                futures.add(executor.submit(() -> {
                    // 模拟计算（斐波那契）
                    return fibonacci(20 + (i % 5));
                }))
        );

        // 收集结果
        for (Future<Integer> f : futures) {
            try {
                bh.consume(f.get()); // 使用 Blackhole 避免结果被优化掉
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 递归斐波那契，用于制造一定的 CPU 计算压力
    private int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
