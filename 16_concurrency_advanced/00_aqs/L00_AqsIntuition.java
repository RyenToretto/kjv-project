import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * [主题]: AQS 直觉 —— 独占锁状态、等待队列、park/unpark
 * [难度]: Hard
 * [前置]: 04_concurrency
 *
 * 一、主题说明
 * AbstractQueuedSynchronizer 是 ReentrantLock / CountDownLatch / Semaphore 的骨架：
 * state + CLH 变体队列 + LockSupport.park。本课实现「迷你独占锁」帮助建立心智，
 * 不是完整 AQS（无条件队列、无可重入计数细节的完整移植）。
 *
 * 二、写法对比
 * 1. 直观版：synchronized。
 * 2. 进阶版：CAS 抢 state，失败则入队 park；解锁 unpark 队头。
 *
 * 三、真实用途
 * - 读 JUC 源码的钥匙；排查「锁谁持有」时知道底层结构。
 */
public class L00_AqsIntuition {

    static class MiniMutex {
        private final AtomicBoolean locked = new AtomicBoolean(false);
        private final ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();

        void lock() {
            while (!locked.compareAndSet(false, true)) {
                waiters.offer(Thread.currentThread());
                LockSupport.park(this);
                waiters.remove(Thread.currentThread());
            }
        }

        void unlock() {
            locked.set(false);
            Thread w = waiters.peek();
            if (w != null) {
                LockSupport.unpark(w);
            }
        }
    }

    public static int syncAdd(int threads, int per) throws InterruptedException {
        MiniMutex lock = new MiniMutex();
        int[] box = {0};
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < per; j++) {
                    lock.lock();
                    try {
                        box[0]++;
                    } finally {
                        lock.unlock();
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

    public static int syncAddBuiltin(int threads, int per) throws InterruptedException {
        Object lock = new Object();
        int[] box = {0};
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < per; j++) {
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

    public static void runTests() throws Exception {
        int expect = 4 * 2000;
        if (syncAddBuiltin(4, 2000) != expect) {
            throw new AssertionError("synchronized 累加失败");
        }
        if (syncAdd(4, 2000) != expect) {
            throw new AssertionError("MiniMutex 累加失败");
        }
        System.out.println("✅ AqsIntuition 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 AqsIntuition ************\n");
        try {
            System.out.println("mini=" + syncAdd(2, 1000));
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
