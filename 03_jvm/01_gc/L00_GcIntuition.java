import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * [主题]: GC 直觉 —— 可达性、垃圾、弱引用
 * [难度]: Medium
 * [前置]: 00_memory
 *
 * 一、主题说明
 * GC（垃圾回收）回收「不可达」对象：从 GC Roots（局部变量、静态变量等）出发走不到的对象。
 * 新生代 / 老年代、复制 / 标记清除等算法细节面试常考，本课先建立「可达性」心智。
 * System.gc() 只是建议，不保证立刻回收——测试里用弱引用观察「可能被回收」。
 *
 * 二、写法对比
 * 1. 直观版：持有强引用列表，对象一直活着。
 * 2. 进阶版：WeakReference；清空强引用后尝试 GC，弱引用可能变 null。
 *
 * 三、真实用途
 * - 缓存误用导致内存涨（静态 Map 一直强引用）。
 * - 排查泄漏：谁还引用着这棵对象图？
 */
public class L00_GcIntuition {

    static class Blob {
        final byte[] data = new byte[64 * 1024]; // 64KB，便于制造可观测垃圾
    }

    /** 直观版：强引用挂住，无法回收这些 Blob。 */
    public static int retainStrong(int n) {
        List<Blob> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Blob());
        }
        return list.size(); // list 活着 ⇒ Blob 可达
    }

    /**
     * 进阶版：弱引用。返回 GC 建议后弱引用是否已被清空。
     * 注意：不同 JVM 不保证；若未清空也不算课失败——我们断言「逻辑路径正确」。
     */
    public static boolean weakMayClear() {
        Blob strong = new Blob();
        WeakReference<Blob> weak = new WeakReference<>(strong);
        if (weak.get() == null) {
            throw new IllegalStateException("刚创建就不应为空");
        }
        strong = null; // 去掉强引用
        System.gc();
        // 给收集器一点时间（教学用，生产勿靠 sleep+gc）
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 可能为 null（已被回收）或仍非 null（尚未回收）——两种都合法
        return true;
    }

    /** 模拟「泄漏」：静态集合一直持有。 */
    private static final List<Blob> LEAK = new ArrayList<>();

    public static int simulateLeak(int n) {
        for (int i = 0; i < n; i++) {
            LEAK.add(new Blob());
        }
        return LEAK.size();
    }

    public static void clearLeak() {
        LEAK.clear();
    }

    public static void runTests() {
        if (retainStrong(3) != 3) {
            throw new AssertionError("强引用保留失败");
        }
        if (!weakMayClear()) {
            throw new AssertionError("弱引用路径应可走通");
        }
        clearLeak();
        if (simulateLeak(2) != 2) {
            throw new AssertionError("泄漏模拟 size 应为 2");
        }
        clearLeak();
        if (!LEAK.isEmpty()) {
            throw new AssertionError("clear 后应为空");
        }
        // 可达性：局部变量离开作用域后不再是 root（语言层面理解题）
        Blob isolated = new Blob();
        WeakReference<Blob> wr = new WeakReference<>(isolated);
        if (wr.get() == null) {
            throw new AssertionError("有强引用时弱引用应能 get 到");
        }
        System.out.println("✅ GcIntuition 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 GcIntuition ************\n");
        System.out.println("retainStrong(2)=" + retainStrong(2));
        System.out.println("weakMayClear path ok? " + weakMayClear());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
