package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 基准测试：演示 LockSupport 的 park/unpark
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)       // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LockSupportExample {

    private Thread worker;

    @Setup(Level.Invocation)
    public void setup() {
        // 每次基准方法执行前，创建一个线程用于 park()
        worker = new Thread(() -> {
            LockSupport.park(); // 阻塞等待 unpark
        });
        worker.start();
    }

    @TearDown(Level.Invocation)
    public void tearDown() throws InterruptedException {
        worker.join();
    }

    /**
     * 基准测试：unpark 唤醒线程
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testLockSupport(Blackhole bh) {
        // 唤醒 worker 线程
        LockSupport.unpark(worker);

        // 消耗一个结果，防止优化
        bh.consume(worker.getState());
    }
}
