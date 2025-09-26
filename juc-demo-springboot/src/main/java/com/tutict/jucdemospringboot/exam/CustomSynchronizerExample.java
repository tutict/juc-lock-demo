package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基准测试：自定义同步器 (基于 AQS 的互斥锁)
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)     // 吞吐量模式
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CustomSynchronizerExample {

    private final Lock customLock = new CustomMutex();

    /**
     * 基于 AQS 的自定义互斥锁
     */
    static class CustomMutex implements Lock {
        // 内部同步器
        private static class Sync extends AbstractQueuedSynchronizer {
            @Override
            protected boolean tryAcquire(int arg) {
                if (compareAndSetState(0, 1)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }

            @Override
            protected boolean tryRelease(int arg) {
                if (getState() == 0) {
                    throw new IllegalMonitorStateException();
                }
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }

            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1 && getExclusiveOwnerThread() == Thread.currentThread();
            }

            Condition newCondition() {
                return new ConditionObject();
            }
        }

        private final Sync sync = new Sync();

        @Override
        public void lock() {
            sync.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(time));
        }

        @Override
        public void unlock() {
            sync.release(1);
        }

        @Override
        public Condition newCondition() {
            return sync.newCondition();
        }
    }

    /**
     * 基准测试：模拟临界区操作
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    @Threads(8) // 模拟多线程竞争
    public void testCustomMutex(Blackhole bh) {
        customLock.lock();
        try {
            // 模拟临界区计算
            int result = 1 + 2;
            bh.consume(result);
        } finally {
            customLock.unlock();
        }
    }
}
