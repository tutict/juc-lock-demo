package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

/**
 * 基准测试：演示 VarHandle 的原子操作
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VarHandleExample {

    private static VarHandle VALUE_HANDLE;
    private volatile int value = 0;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // 绑定 VarHandle 到字段 value
        VALUE_HANDLE = MethodHandles.lookup()
                .in(VarHandleExample.class)
                .findVarHandle(VarHandleExample.class, "value", int.class);
    }

    /**
     * 基准方法：原子增加
     */
    @Benchmark
    @Fork(1)                     // 启动 1 个 JVM
    @Warmup(iterations = 2)       // 预热 2 次
    @Measurement(iterations = 3)  // 实测 3 次
    public void testAtomicIncrement(Blackhole bh) {
        int oldVal, newVal;
        do {
            oldVal = (int) VALUE_HANDLE.getVolatile(this);
            newVal = oldVal + 1;
        } while (!VALUE_HANDLE.compareAndSet(this, oldVal, newVal));
        bh.consume(newVal);
    }

    /**
     * 基准方法：普通 volatile 读写（对比用）
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public void testVolatileIncrement(Blackhole bh) {
        value++;
        bh.consume(value);
    }
}
