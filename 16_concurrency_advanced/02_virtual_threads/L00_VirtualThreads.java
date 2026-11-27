import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: 虚拟线程（JDK 21+，本仓库按 25）—— 大量阻塞任务的轻量并发
 * [难度]: Medium～Hard
 * [前置]: 04_pool
 *
 * 一、主题说明
 * 虚拟线程由 JVM 调度挂到载体线程；适合大量阻塞 I/O，不适合长时间占 CPU 的重计算。
 * API：Thread.ofVirtual()、Executors.newVirtualThreadPerTaskExecutor()。
 *
 * 二、写法对比
 * 1. 直观版：固定平台线程池跑很多 sleep 任务（易排队）。
 * 2. 进阶版：虚拟线程每任务一个，完成计数。
 *
 * 三、真实用途
 * - Tomcat/Jetty 虚拟线程模式；Spring Boot 3.2+ 可开启。
 */
public class L00_VirtualThreads {

    public static int runPlatformPool(int tasks, int poolSize) throws InterruptedException {
        AtomicInteger done = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    sleep(5);
                    done.incrementAndGet();
                });
            }
            pool.shutdown();
            while (!pool.isTerminated()) {
                Thread.sleep(1);
            }
        }
        return done.get();
    }

    public static int runVirtual(int tasks) throws InterruptedException {
        AtomicInteger done = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    sleep(5);
                    done.incrementAndGet();
                });
            }
            pool.shutdown();
            while (!pool.isTerminated()) {
                Thread.sleep(1);
            }
        }
        return done.get();
    }

    public static boolean currentIsVirtual() throws InterruptedException {
        boolean[] flag = {false};
        Thread t = Thread.ofVirtual().start(() -> flag[0] = Thread.currentThread().isVirtual());
        t.join();
        return flag[0];
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void runTests() throws Exception {
        if (runPlatformPool(20, 4) != 20) {
            throw new AssertionError("平台池任务数");
        }
        if (runVirtual(50) != 50) {
            throw new AssertionError("虚拟线程任务数");
        }
        if (!currentIsVirtual()) {
            throw new AssertionError("应是虚拟线程");
        }
        if (Thread.currentThread().isVirtual()) {
            throw new AssertionError("main 通常不是虚拟线程");
        }
        System.out.println("✅ VirtualThreads 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 VirtualThreads ************\n");
        try {
            System.out.println("virtual done=" + runVirtual(10));
            System.out.println("isVirtual demo=" + currentIsVirtual());
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
