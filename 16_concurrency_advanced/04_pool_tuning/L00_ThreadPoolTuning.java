import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: ThreadPoolExecutor 调参 —— 核心数、队列、拒绝策略
 * [难度]: Hard
 * [前置]: 04_pool
 *
 * 一、主题说明
 * Executors 工厂可能隐藏无界队列风险。生产手写：
 * core / max / keepAlive / 有界队列 / Abort/CallerRuns/Discard。
 * 公式直觉：CPU 密集 ≈ Ncpu+1；I/O 密集可更大，但要压测。
 *
 * 二、写法对比
 * 1. 直观版：newFixedThreadPool（无界 LinkedBlockingQueue）。
 * 2. 进阶版：有界队列 + AbortPolicy，提交过载立即失败。
 *
 * 三、真实用途
 * - 保护下游；熔断前的本地限流；监控队列长度与拒绝次数。
 */
public class L00_ThreadPoolTuning {

    public static ThreadPoolExecutor boundedAbort(int core, int max, int queueCap) {
        return new ThreadPoolExecutor(
                core, max, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCap),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor boundedCallerRuns(int core, int max, int queueCap) {
        return new ThreadPoolExecutor(
                core, max, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCap),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static int fillUntilReject(ThreadPoolExecutor pool, int attempts) {
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        for (int i = 0; i < attempts; i++) {
            try {
                pool.execute(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                accepted.incrementAndGet();
            } catch (RejectedExecutionException ex) {
                rejected.incrementAndGet();
            }
        }
        return rejected.get();
    }

    public static void runTests() throws Exception {
        ThreadPoolExecutor abort = boundedAbort(1, 1, 1);
        try {
            int rejected = fillUntilReject(abort, 5);
            if (rejected < 1) {
                throw new AssertionError("有界+Abort 应出现拒绝");
            }
        } finally {
            abort.shutdownNow();
        }

        ThreadPoolExecutor caller = boundedCallerRuns(1, 1, 1);
        try {
            // CallerRuns：不会抛拒绝，调用线程自己跑
            for (int i = 0; i < 3; i++) {
                caller.execute(() -> {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } finally {
            caller.shutdown();
            caller.awaitTermination(2, TimeUnit.SECONDS);
        }

        // 参数只读检查
        ThreadPoolExecutor p = boundedAbort(2, 4, 10);
        try {
            if (p.getCorePoolSize() != 2 || p.getMaximumPoolSize() != 4) {
                throw new AssertionError("池参数不对");
            }
            if (!(p.getQueue() instanceof ArrayBlockingQueue<?> q) || q.remainingCapacity() + q.size() != 10) {
                throw new AssertionError("队列容量应为 10");
            }
        } finally {
            p.shutdownNow();
        }
        System.out.println("✅ ThreadPoolTuning 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ThreadPoolTuning ************\n");
        try {
            ThreadPoolExecutor p = boundedAbort(1, 2, 2);
            System.out.println("rejected in demo=" + fillUntilReject(p, 6));
            p.shutdownNow();
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
