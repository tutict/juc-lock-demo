package com.tutict.jucdemospringboot.exam.pool;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 基准测试：对比 CachedThreadPool 和 VirtualThreadPool 执行并发任务
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)       // 每毫秒能执行多少次
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CachedThreadPoolBenchmark {

    private ExecutorService cachedThreadPool;
    private ExecutorService virtualThreadPool;

    @Setup(Level.Iteration)
    public void setup() {
        cachedThreadPool = Executors.newCachedThreadPool();
        virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        cachedThreadPool.shutdown();
        virtualThreadPool.shutdown();
        cachedThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        virtualThreadPool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 模拟提交 100 个耗时任务（每个任务 sleep 10ms）
     */
    private void runTasks(ExecutorService executor, Blackhole bh) {
        List<Callable<String>> tasks = IntStream.range(0, 100)
                .mapToObj(i -> (Callable<String>) () -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(10); // 模拟 IO
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    String result = "task-" + i;
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
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testCachedThreadPool(Blackhole bh) {
        runTasks(cachedThreadPool, bh);
    }

    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVirtualThreadPool(Blackhole bh) {
        runTasks(virtualThreadPool, bh);
    }
}
