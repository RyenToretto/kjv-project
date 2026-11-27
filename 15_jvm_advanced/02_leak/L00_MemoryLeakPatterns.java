import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: 常见内存泄漏模式 —— 静态集合、未关闭、监听器、ThreadLocal
 * [难度]: Hard
 * [前置]: 03_gc、03_troubleshooting
 *
 * 一、主题说明
 * Java 有 GC 仍会「逻辑泄漏」：对象仍可达但业务不再需要。
 * 本课演示静态 Map 涨、ThreadLocal 未 remove（线程池复用时尤其危险）。
 *
 * 二、写法对比
 * 1. 直观版：静态 cache 只 put 不淘汰。
 * 2. 进阶版：弱引用 / 显式 clear / ThreadLocal remove。
 *
 * 三、真实用途
 * - MAT 查 Dominator Tree；线上 heap dump。
 */
public class L00_MemoryLeakPatterns {

    static final Map<String, byte[]> STATIC_CACHE = new HashMap<>();

    static final ThreadLocal<byte[]> TL = new ThreadLocal<>();

    public static int leakStatic(int n) {
        for (int i = 0; i < n; i++) {
            STATIC_CACHE.put("k" + i, new byte[1024]);
        }
        return STATIC_CACHE.size();
    }

    public static void clearStatic() {
        STATIC_CACHE.clear();
    }

    public static int boundedPut(Map<String, byte[]> cache, int max, String k, byte[] v) {
        cache.put(k, v);
        if (cache.size() > max) {
            String first = cache.keySet().iterator().next();
            cache.remove(first);
        }
        return cache.size();
    }

    public static boolean threadLocalLeakDemo() {
        TL.set(new byte[2048]);
        // 忘记 remove → 线程池场景泄漏；这里演示 get 仍在
        boolean present = TL.get() != null;
        TL.remove();
        return present && TL.get() == null;
    }

    public static boolean weakAllowsGc() {
        Object obj = new byte[1024];
        WeakReference<Object> wr = new WeakReference<>(obj);
        if (wr.get() == null) {
            return false;
        }
        obj = null;
        System.gc();
        // 不保证立刻回收；返回「路径正确」
        return true;
    }

    public static void runTests() {
        clearStatic();
        if (leakStatic(5) != 5) {
            throw new AssertionError("静态泄漏 size");
        }
        clearStatic();
        if (!STATIC_CACHE.isEmpty()) {
            throw new AssertionError("clear 后应空");
        }
        Map<String, byte[]> bounded = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            boundedPut(bounded, 3, "k" + i, new byte[8]);
        }
        if (bounded.size() != 3) {
            throw new AssertionError("有界缓存应保持 3, size=" + bounded.size());
        }
        if (!threadLocalLeakDemo()) {
            throw new AssertionError("ThreadLocal remove 失败");
        }
        if (!weakAllowsGc()) {
            throw new AssertionError("弱引用路径");
        }
        List<WeakReference<Object>> list = new ArrayList<>();
        list.add(new WeakReference<>(new Object()));
        if (list.size() != 1) {
            throw new AssertionError("list");
        }
        System.out.println("✅ MemoryLeakPatterns 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 MemoryLeakPatterns ************\n");
        System.out.println("leakStatic(3)=" + leakStatic(3));
        clearStatic();
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
