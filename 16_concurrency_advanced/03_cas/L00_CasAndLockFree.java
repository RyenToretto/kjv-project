import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * [主题]: CAS 与无锁 —— AtomicInteger / Treiber Stack
 * [难度]: Hard
 * [前置]: 00_aqs、04 Atomic
 *
 * 一、主题说明
 * CAS（Compare-And-Set）：CPU 原子指令，成功才更新。
 * ABA 问题：值变回原样会误判成功；可用 AtomicStampedReference。
 * 本课实现无锁栈（Treiber）演示 CAS 循环。
 *
 * 二、写法对比
 * 1. 直观版：synchronized 栈。
 * 2. 进阶版：AtomicReference CAS 头指针。
 *
 * 三、真实用途
 * - JUC 底层；高并发计数；无锁结构需谨慎（进度保证、ABA）。
 */
public class L00_CasAndLockFree {

    static class SyncStack {
        private Node head;

        synchronized void push(int v) {
            head = new Node(v, head);
        }

        synchronized Integer pop() {
            if (head == null) {
                return null;
            }
            int v = head.v;
            head = head.next;
            return v;
        }
    }

    static class LockFreeStack {
        private final AtomicReference<Node> head = new AtomicReference<>();

        void push(int v) {
            Node n = new Node(v, null);
            while (true) {
                Node h = head.get();
                n.next = h;
                if (head.compareAndSet(h, n)) {
                    return;
                }
            }
        }

        Integer pop() {
            while (true) {
                Node h = head.get();
                if (h == null) {
                    return null;
                }
                if (head.compareAndSet(h, h.next)) {
                    return h.v;
                }
            }
        }
    }

    static class Node {
        final int v;
        Node next;

        Node(int v, Node next) {
            this.v = v;
            this.next = next;
        }
    }

    public static int casCount(int threads, int per) throws InterruptedException {
        AtomicInteger n = new AtomicInteger();
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < per; j++) {
                    n.incrementAndGet();
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        return n.get();
    }

    public static void runTests() throws Exception {
        if (casCount(8, 1000) != 8000) {
            throw new AssertionError("CAS 计数失败");
        }
        SyncStack s = new SyncStack();
        s.push(1);
        s.push(2);
        if (s.pop() != 2 || s.pop() != 1 || s.pop() != null) {
            throw new AssertionError("同步栈失败");
        }
        LockFreeStack lf = new LockFreeStack();
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) {
            int base = i * 100;
            ts[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    lf.push(base + j);
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        int count = 0;
        while (lf.pop() != null) {
            count++;
        }
        if (count != 400) {
            throw new AssertionError("无锁栈元素应为 400, 实际=" + count);
        }
        System.out.println("✅ CasAndLockFree 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CasAndLockFree ************\n");
        try {
            System.out.println("casCount=" + casCount(4, 100));
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
