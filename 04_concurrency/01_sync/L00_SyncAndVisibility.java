import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: synchronized 互斥与可见性入门
 * [难度]: Medium～Hard
 * [前置]: 00_thread
 *
 * 一、主题说明
 * 多线程同时读写共享变量会出现竞态（race）。
 * synchronized：同一监视器上互斥进入；同时建立 happens-before，保证可见性。
 * volatile：保证可见性与禁止部分重排序，但不保证复合操作原子性（i++ 仍要锁或 Atomic）。
 *
 * 二、写法对比
 * 1. 直观版：无锁累加（结果常偏小，测试里用多次尝试或 Atomic 对照）。
 * 2. 进阶版：synchronized 或 AtomicInteger。
 *
 * 三、真实用途
 * - 库存扣减、计数器、单例双重检查（进阶）。
 * - 能用 java.util.concurrent 尽量不用自己玩锁。
 */
public class L00_SyncAndVisibility {

    /** 无锁：演示「不该这么干」。 */
    public static int unsafeAdd(int threads, int perThread) throws InterruptedException {
        int[] box = {0}; // 用数组包装以便匿名类写入
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    box[0]++; // 非原子
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        return box[0];
    }

    /** synchronized 保护。 */
    public static int safeAddSync(int threads, int perThread) throws InterruptedException {
        int[] box = {0};
        Object lock = new Object();
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    synchronized (lock) {
                        box[0]++;
                    }
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        return box[0];
    }

    /** AtomicInteger。 */
    public static int safeAddAtomic(int threads, int perThread) throws InterruptedException {
        AtomicInteger box = new AtomicInteger();
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    box.incrementAndGet();
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        return box.get();
    }

    public static void runTests() {
        try {
            int expect = 8 * 1000;
            if (safeAddSync(8, 1000) != expect) {
                throw new AssertionError("synchronized 累加应精确");
            }
            if (safeAddAtomic(8, 1000) != expect) {
                throw new AssertionError("Atomic 累加应精确");
            }
            int unsafe = unsafeAdd(8, 1000);
            // 极低概率碰巧正确；若相等也接受，但打印提示
            if (unsafe > expect) {
                throw new AssertionError("无锁结果不应超过期望");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("中断", e);
        }
        System.out.println("✅ SyncAndVisibility 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SyncAndVisibility ************\n");
        try {
            System.out.println("unsafe≈" + unsafeAdd(4, 5000));
            System.out.println("sync =" + safeAddSync(4, 5000));
            System.out.println("atomic=" + safeAddAtomic(4, 5000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
