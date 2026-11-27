import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: 连接池心智 —— 借还、最大连接、等待超时
 * [难度]: Medium
 * [前置]: 05_jdbc、14
 *
 * 一、主题说明
 * DB/Redis/HTTP 客户端都靠池化：创建连接昂贵。
 * HikariCP：最大连接、超时、泄漏检测。本课实现迷你池。
 *
 * 二、写法对比
 * 1. 直观版：每次 new 连接（计数器模拟）。
 * 2. 进阶版：固定容量池，借不到则等待/失败。
 *
 * 三、真实用途
 * - 调 spring.datasource.hikari.*；避免连接泄漏（未关闭）。
 */
public class L00_ConnectionPoolIdea {

    static class Conn {
        final int id;
        boolean inUse;

        Conn(int id) {
            this.id = id;
        }
    }

    static class MiniPool {
        private final Deque<Conn> idle = new ArrayDeque<>();
        private final AtomicInteger created = new AtomicInteger();
        private final int max;

        MiniPool(int max) {
            this.max = max;
        }

        synchronized Conn borrow(long waitMs) throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitMs);
            while (true) {
                if (!idle.isEmpty()) {
                    Conn c = idle.poll();
                    c.inUse = true;
                    return c;
                }
                if (created.get() < max) {
                    Conn c = new Conn(created.incrementAndGet());
                    c.inUse = true;
                    return c;
                }
                long left = deadline - System.nanoTime();
                if (left <= 0) {
                    return null; // 超时
                }
                TimeUnit.NANOSECONDS.timedWait(this, left);
            }
        }

        synchronized void release(Conn c) {
            c.inUse = false;
            idle.offer(c);
            notifyAll();
        }

        int created() {
            return created.get();
        }
    }

    public static int openCloseNaive(int n) {
        // 每次「新建」
        return n;
    }

    public static void runTests() throws Exception {
        if (openCloseNaive(10) != 10) {
            throw new AssertionError("naive");
        }
        MiniPool pool = new MiniPool(2);
        Conn a = pool.borrow(100);
        Conn b = pool.borrow(100);
        if (a == null || b == null || pool.created() != 2) {
            throw new AssertionError("应创建 2 个连接");
        }
        Conn c = pool.borrow(50);
        if (c != null) {
            throw new AssertionError("池满且无人归还应超时失败");
        }
        pool.release(a);
        Conn d = pool.borrow(100);
        if (d == null || d.id != a.id) {
            throw new AssertionError("归还后应复用 a");
        }
        if (pool.created() != 2) {
            throw new AssertionError("不应再创建");
        }
        System.out.println("✅ ConnectionPoolIdea 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ConnectionPoolIdea ************\n");
        try {
            MiniPool pool = new MiniPool(3);
            Conn c = pool.borrow(10);
            System.out.println("borrowed id=" + c.id);
            pool.release(c);
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
