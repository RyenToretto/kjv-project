import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * [主题]: CompletableFuture 编排 —— thenCombine / allOf / 异常恢复
 * [难度]: Hard
 * [前置]: 04_pool、01_lambda
 *
 * 一、主题说明
 * 异步编排替代回调地狱：供应 → 转换 → 合并 → 处理异常。
 * 注意默认 ForkJoinPool.commonPool；生产常自备线程池。
 *
 * 二、写法对比
 * 1. 直观版：串行阻塞调用三个下游。
 * 2. 进阶版：并行 CF 合并结果。
 *
 * 三、真实用途
 * - 聚合多个 HTTP/RPC；网关 BFF。
 */
public class L00_CompletableFutureOrchestration {

    public static int priceNaive() throws InterruptedException {
        int a = slow(10);
        int b = slow(20);
        int c = slow(30);
        return a + b + c;
    }

    public static int priceBetter(ExecutorService pool) throws Exception {
        CompletableFuture<Integer> fa = CompletableFuture.supplyAsync(() -> slowUnchecked(10), pool);
        CompletableFuture<Integer> fb = CompletableFuture.supplyAsync(() -> slowUnchecked(20), pool);
        CompletableFuture<Integer> fc = CompletableFuture.supplyAsync(() -> slowUnchecked(30), pool);
        return fa.thenCombine(fb, Integer::sum).thenCombine(fc, Integer::sum).get(2, TimeUnit.SECONDS);
    }

    public static String recoverDemo(ExecutorService pool) throws Exception {
        return CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("boom");
                }, pool)
                .exceptionally(ex -> "fallback:" + ex.getCause().getMessage())
                .get(1, TimeUnit.SECONDS);
    }

    public static int allOfSum(ExecutorService pool) throws Exception {
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 1, pool);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 2, pool);
        CompletableFuture<Void> all = CompletableFuture.allOf(a, b);
        all.get(1, TimeUnit.SECONDS);
        return a.get() + b.get();
    }

    private static int slow(int v) throws InterruptedException {
        Thread.sleep(30);
        return v;
    }

    private static int slowUnchecked(int v) {
        try {
            return slow(v);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public static void runTests() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            if (priceNaive() != 60) {
                throw new AssertionError("naive sum");
            }
            if (priceBetter(pool) != 60) {
                throw new AssertionError("cf sum");
            }
            long t0 = System.nanoTime();
            priceBetter(pool);
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            // 串行约 90ms，并行约 30ms+；机器忙时放宽到 120ms
            if (ms >= 120) {
                throw new AssertionError("并行应明显快于串行(~90ms), ms=" + ms);
            }
            if (!recoverDemo(pool).startsWith("fallback:")) {
                throw new AssertionError("exceptionally 失败");
            }
            if (allOfSum(pool) != 3) {
                throw new AssertionError("allOf 失败");
            }
        } finally {
            pool.shutdownNow();
        }
        System.out.println("✅ CompletableFutureOrchestration 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CompletableFutureOrchestration ************\n");
        try {
            ExecutorService pool = Executors.newFixedThreadPool(3);
            System.out.println("parallel sum=" + priceBetter(pool));
            pool.shutdownNow();
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
