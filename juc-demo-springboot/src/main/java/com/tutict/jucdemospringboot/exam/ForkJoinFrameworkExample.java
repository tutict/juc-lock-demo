package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * 基准测试：演示 Fork/Join 框架的分治计算
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)        // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoinFrameworkExample {

    private ForkJoinPool forkJoinPool;
    private long[] array;

    @Setup(Level.Iteration)
    public void setup() {
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        // 准备一个大数组
        array = LongStream.rangeClosed(1, 1_000_000).toArray();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        forkJoinPool.shutdown();
    }

    /**
     * Fork/Join 任务：分治求和
     */
    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10_000;
        private final long[] array;
        private final int start, end;

        SumTask(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }

            int mid = start + length / 2;
            SumTask leftTask = new SumTask(array, start, mid);
            SumTask rightTask = new SumTask(array, mid, end);

            leftTask.fork();                  // 异步执行子任务
            long rightResult = rightTask.compute(); // 当前线程执行右子任务
            long leftResult = leftTask.join();      // 等待左子任务结果

            return leftResult + rightResult;
        }
    }

    /**
     * 基准测试：使用 ForkJoinPool 求和
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testForkJoin(Blackhole bh) {
        SumTask task = new SumTask(array, 0, array.length);
        long result = forkJoinPool.invoke(task);
        bh.consume(result);
    }
}
