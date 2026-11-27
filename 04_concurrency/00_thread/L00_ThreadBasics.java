/**
 * [主题]: 线程基础 —— Thread / Runnable / 休眠与加入
 * [难度]: Medium
 * [前置]: 00_java_basics、03_jvm（栈每线程一份）
 *
 * 一、主题说明
 * 线程是 CPU 调度的基本单位。Java 里可 extends Thread 或实现 Runnable（更推荐后者）。
 * start() 才是启动新线程；直接 run() 只在当前线程执行。
 *
 * 二、写法对比
 * 1. 直观版：匿名 Thread 子类。
 * 2. 进阶版：Runnable / 线程名；join 等待结束。
 *
 * 三、真实用途
 * - 异步发短信、并行计算；现代业务更常用线程池（下一课之后）。
 */
public class L00_ThreadBasics {

    /** 直观版：子类 Thread。返回子线程是否真的跑过。 */
    public static boolean runWithSubclass(java.util.concurrent.atomic.AtomicBoolean flag)
            throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                flag.set(true);
            }
        };
        t.start();
        t.join(2000);
        return flag.get();
    }

    /** 进阶版：Runnable + 命名。 */
    public static String runWithRunnable() throws InterruptedException {
        StringBuilder who = new StringBuilder();
        Thread t = new Thread(() -> who.append(Thread.currentThread().getName()), "worker-demo");
        t.start();
        t.join(2000);
        return who.toString();
    }

    /** 错误示范检测：只调 run 不 start，标志应在主线程被设置且线程名不含 worker。 */
    public static String callRunDirectly() {
        StringBuilder who = new StringBuilder();
        Thread t = new Thread(() -> who.append(Thread.currentThread().getName()), "worker-demo");
        t.run(); // 不启动新线程
        return who.toString();
    }

    public static void runTests() {
        try {
            java.util.concurrent.atomic.AtomicBoolean flag =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            if (!runWithSubclass(flag)) {
                throw new AssertionError("子类线程应置位 flag");
            }
            String name = runWithRunnable();
            if (!"worker-demo".equals(name)) {
                throw new AssertionError("应在 worker-demo 线程执行, 实际=" + name);
            }
            String direct = callRunDirectly();
            if ("worker-demo".equals(direct)) {
                throw new AssertionError("直接 run() 不应切换到 worker-demo");
            }
            if (direct.isEmpty()) {
                throw new AssertionError("直接 run() 仍应执行体");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("测试被中断", e);
        }
        System.out.println("✅ ThreadBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ThreadBasics ************\n");
        try {
            System.out.println("runnable thread=" + runWithRunnable());
            System.out.println("direct run thread=" + callRunDirectly());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
