package com.tutict.jucdemospringboot.exam;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * 基准测试：对比 ConcurrentHashMap 和 SynchronizedMap 的并发性能
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)   // 吞吐量：每毫秒执行的操作数
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ConcurrentHashMapExample {

    private static final Logger logger = Logger.getGlobal();

    private ConcurrentHashMap<Integer, Integer> concurrentMap;
    private Map<Integer, Integer> synchronizedMap;

    private AtomicInteger counter;

    @Setup(Level.Iteration)
    public void setup() {
        concurrentMap = new ConcurrentHashMap<>();
        synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        counter = new AtomicInteger(0);
    }

    /**
     * 模拟 key-value 写入
     */
    private void writeToMap(Map<Integer, Integer> map, Blackhole bh) {
        int key = counter.getAndIncrement();
        map.put(key, key);
        bh.consume(map);
    }

    /**
     * 基准测试：ConcurrentHashMap 写操作
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    @Threads(8)   // 模拟 8 个线程同时执行
    public void testConcurrentHashMapPut(Blackhole bh) {
        writeToMap(concurrentMap, bh);
    }

    /**
     * 基准测试：SynchronizedMap 写操作
     */
    @Benchmark
    @Fork(1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    @Threads(8)   // 模拟 8 个线程同时执行
    public void testSynchronizedMapPut(Blackhole bh) {
        writeToMap(synchronizedMap, bh);
    }
}
