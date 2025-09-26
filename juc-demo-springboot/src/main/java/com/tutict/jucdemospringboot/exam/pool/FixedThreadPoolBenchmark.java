package com.tutict.jucdemospringboot.exam.pool;

import org.openjdk.jmh.annotations.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * 比较固定线程池和虚拟线程池在提交任务时的性能
 */
@State(Scope.Benchmark)
public class FixedThreadPoolBenchmark {

    private static final Logger logger = Logger.getGlobal();

    private ExecutorService fixedThreadPool;
    private ExecutorService virtualThreadPool;

    // 每次基准测试之前初始化
    @Setup(Level.Iteration)
    public void setup() {
        fixedThreadPool = Executors.newFixedThreadPool(10); // 固定线程池
        virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor(); // 虚拟线程池
    }

    // 每次基准测试之后关闭
    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        fixedThreadPool.shutdown();
        virtualThreadPool.shutdown();
        fixedThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        virtualThreadPool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 基准任务：提交 1000 个简单任务
     */
    private void runTasks(ExecutorService executor) {
        List<Callable<String>> tasks = IntStream.range(0, 1000)
                .mapToObj(i -> (Callable<String>) () -> {
                    // 模拟轻量级任务
                    return "task-" + i;
                })
                .toList();

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Benchmark
    @Fork(value = 1)                // 启动一个独立的 JVM 进程运行测试
    @Warmup(iterations = 2)         // 预热2次（预热阶段不计入最终数据）
    @Measurement(iterations = 3)    // 实际测试3次
    public void testFixedThreadPool() {
        runTasks(fixedThreadPool);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVirtualThreadPool() {
        runTasks(virtualThreadPool);
    }
}
