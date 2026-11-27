import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * [主题]: JUC 常用工具 —— ConcurrentHashMap / CountDownLatch / ReentrantLock
 * [难度]: Medium
 * [前置]: 01_sync
 *
 * 一、主题说明
 * java.util.concurrent（JUC）提供更高层并发组件，比手写 synchronized 更不易错。
 * ConcurrentHashMap：并发 Map；CountDownLatch：等待 N 个事件完成；ReentrantLock：显式锁。
 *
 * 二、写法对比
 * 1. 直观版：HashMap + synchronized 整表锁。
 * 2. 进阶版：ConcurrentHashMap；Latch 等待工人线程。
 *
 * 三、真实用途
 * - 本地缓存、统计；并行任务汇合；可中断/定时的锁获取。
 */
public class L00_JucTools {

    public static ConcurrentHashMap<String, Integer> concurrentWordCount(String[] words)
            throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(words.length);
        for (String w : words) {
            new Thread(() -> {
                map.merge(w, 1, Integer::sum);
                latch.countDown();
            }).start();
        }
        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new IllegalStateException("latch 超时");
        }
        return map;
    }

    public static int lockAdd(int times) {
        ReentrantLock lock = new ReentrantLock();
        int[] box = {0};
        Runnable task = () -> {
            for (int i = 0; i < times; i++) {
                lock.lock();
                try {
                    box[0]++;
                } finally {
                    lock.unlock(); // 必须在 finally 释放
                }
            }
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return box[0];
    }

    /** Latch：主线程等 3 个准备工作完成。 */
    public static List<String> waitWorkers() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(3);
        List<String> logs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int id = i;
            new Thread(() -> {
                synchronized (logs) {
                    logs.add("w" + id);
                }
                ready.countDown();
            }).start();
        }
        ready.await(3, TimeUnit.SECONDS);
        return logs;
    }

    public static void runTests() {
        try {
            var map = concurrentWordCount(new String[]{"a", "b", "a", "c", "a"});
            if (map.getOrDefault("a", 0) != 3 || map.getOrDefault("b", 0) != 1) {
                throw new AssertionError("并发词频错误: " + map);
            }
            if (lockAdd(1000) != 2000) {
                throw new AssertionError("ReentrantLock 累加应为 2000");
            }
            List<String> logs = waitWorkers();
            if (logs.size() != 3) {
                throw new AssertionError("应有 3 个 worker 日志");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("中断", e);
        }
        System.out.println("✅ JucTools 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 JucTools ************\n");
        try {
            System.out.println(concurrentWordCount(new String[]{"java", "java", "juc"}));
            System.out.println("lockAdd=" + lockAdd(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
