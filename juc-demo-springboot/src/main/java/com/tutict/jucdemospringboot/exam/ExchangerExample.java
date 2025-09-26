package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 基准测试：演示 Exchanger 在两个线程之间交换数据
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)    // 吞吐量
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ExchangerExample {

    private Exchanger<String> exchanger;
    private ExecutorService executor;

    @Setup(Level.Iteration)
    public void setup() {
        exchanger = new Exchanger<>();
        executor = Executors.newFixedThreadPool(2); // 两个线程进行配对交换
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 使用 Exchanger 让两个线程交换字符串
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testExchanger(Blackhole bh) {
        executor.submit(() -> {
            try {
                String data = "Thread-A";
                String received = exchanger.exchange(data);
                bh.consume(received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.submit(() -> {
            try {
                String data = "Thread-B";
                String received = exchanger.exchange(data);
                bh.consume(received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
