import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: 线程池 —— ExecutorService 基本用法
 * [难度]: Medium
 * [前置]: 00_thread、02_juc
 *
 * 一、主题说明
 * 不要为每个任务 new Thread：创建/销毁昂贵，且无法限流。
 * ExecutorService：复用工作者线程；提交 Runnable/Callable；务必 shutdown。
 * 生产常用 ThreadPoolExecutor 明确核心数/队列/拒绝策略；本课先会用固定池。
 *
 * 二、写法对比
 * 1. 直观版：循环 new Thread。
 * 2. 进阶版：Executors.newFixedThreadPool + Future。
 *
 * 三、真实用途
 * - 并行调用下游、批量处理；Spring 也可注入 TaskExecutor。
 * - 注意：Executors 工厂在大型系统可能隐藏队列无界风险（Alibaba 规约建议手写 ThreadPoolExecutor）。
 */
public class L00_ThreadPool {

    public static int runWithRawThreads(int tasks) throws InterruptedException {
        AtomicInteger done = new AtomicInteger();
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            Thread t = new Thread(done::incrementAndGet);
            list.add(t);
            t.start();
        }
        for (Thread t : list) {
            t.join();
        }
        return done.get();
    }

    public static int runWithPool(int tasks, int poolSize) throws Exception {
        AtomicInteger done = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                futures.add(pool.submit(done::incrementAndGet));
            }
            for (Future<?> f : futures) {
                f.get(3, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
        return done.get();
    }

    /** Callable 有返回值。 */
    public static int sumWithPool(int[] nums, int poolSize) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int n : nums) {
                futures.add(pool.submit(() -> n * n));
            }
            int sum = 0;
            for (Future<Integer> f : futures) {
                sum += f.get();
            }
            return sum;
        } finally {
            pool.shutdownNow();
        }
    }

    public static void runTests() {
        try {
            if (runWithRawThreads(20) != 20) {
                throw new AssertionError("裸线程任务数不对");
            }
            if (runWithPool(50, 4) != 50) {
                throw new AssertionError("线程池任务数不对");
            }
            if (sumWithPool(new int[]{1, 2, 3, 4}, 2) != 1 + 4 + 9 + 16) {
                throw new AssertionError("平方和错误");
            }
        } catch (Exception e) {
            throw new AssertionError("线程池测试失败: " + e.getMessage(), e);
        }
        System.out.println("✅ ThreadPool 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ThreadPool ************\n");
        try {
            System.out.println("pool done=" + runWithPool(10, 2));
            System.out.println("sum squares=" + sumWithPool(new int[]{3, 4}, 2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
