package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * 基准测试：演示 Phaser 分阶段同步
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)        // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PhaserExample {

    private Phaser phaser;

    @Setup(Level.Iteration)
    public void setup() {
        // 初始化 Phaser，初始注册 3 个参与者
        phaser = new Phaser(3);
    }

    /**
     * 基准方法：模拟 3 个线程在同一阶段同步
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testPhaser(Blackhole bh) {
        Thread t1 = new Thread(new Worker(phaser, bh), "T1");
        Thread t2 = new Thread(new Worker(phaser, bh), "T2");
        Thread t3 = new Thread(new Worker(phaser, bh), "T3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 工作线程：执行两阶段任务
     */
    static class Worker implements Runnable {
        private final Phaser phaser;
        private final Blackhole bh;

        Worker(Phaser phaser, Blackhole bh) {
            this.phaser = phaser;
            this.bh = bh;
        }

        @Override
        public void run() {
            // 阶段 1
            doWork("阶段1");
            phaser.arriveAndAwaitAdvance(); // 等待其他线程到达

            // 阶段 2
            doWork("阶段2");
            phaser.arriveAndDeregister();   // 任务完成，解除注册
        }

        private void doWork(String phase) {
            bh.consume(Thread.currentThread().getName() + " 执行 " + phase);
        }
    }
}
