package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 基准测试：对比串行计算与 CompletableFuture 异步计算
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)   // 吞吐量模式：每毫秒执行多少操作
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CompletableFutureExample {

    private static final Logger logger = Logger.getGlobal();

    private ExecutorService threadPool;

    @Setup(Level.Iteration)
    public void setup() {
        // 固定线程池用于 CompletableFuture
        threadPool = Executors.newFixedThreadPool(4);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        threadPool.shutdown();
    }

    /**
     * 模拟 CPU 计算任务
     */
    private int compute(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    /**
     * 基准测试：串行执行任务
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testSequential(Blackhole bh) {
        int result = compute(1000);
        bh.consume(result);
    }

    /**
     * 基准测试：CompletableFuture 异步执行任务
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testCompletableFutureAsync(Blackhole bh) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(
                () -> compute(1000), threadPool
        );
        int result = future.get(); // 等待结果
        bh.consume(result);
    }
}
