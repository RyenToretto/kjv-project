/**
 * [主题]: 类加载直觉 —— Class 对象、首次主动使用
 * [难度]: Medium
 * [前置]: 00_memory
 *
 * 一、主题说明
 * 类加载大致：加载 → 链接（验证/准备/解析）→ 初始化（执行 &lt;clinit&gt;）。
 * 同一份字节码由同一个 ClassLoader 加载，得到唯一 Class 对象（在该 loader 命名空间内）。
 * 本课用「静态块是否执行」观察初始化时机；用 class 字面量拿 Class。
 *
 * 二、写法对比
 * 1. 直观版：直接 new，触发初始化。
 * 2. 进阶版：Class.forName；对比 getName / 类字面量。
 *
 * 三、真实用途
 * - SPI、热部署、Tomcat/Spring 多层 ClassLoader（进阶）。
 * - 静态字段初始化异常会导致 ExceptionInInitializerError。
 */
public class L00_ClassLoading {

    static class Counter {
        static int initCount = 0;

        static {
            initCount++;
        }

        static String hello() {
            return "hi-" + initCount;
        }
    }

    static class Lazy {
        static {
            LazyMarker.initialized = true;
        }
    }

    static class LazyMarker {
        static boolean initialized = false;
    }

    /** 触发 Counter 初始化并返回 initCount。 */
    public static int touchCounter() {
        return Counter.initCount > 0 ? Counter.initCount : trigger();
    }

    private static int trigger() {
        Counter.hello();
        return Counter.initCount;
    }

    public static String classNameOf() {
        return Counter.class.getName();
    }

    public static String forNameHello() throws ClassNotFoundException {
        Class<?> clazz = Class.forName(Counter.class.getName());
        // 已初始化过则不会重复跑 static（同一 loader）
        return clazz.getSimpleName();
    }

    /** 仅加载类字面量不一定初始化（依赖场景）；这里用 forName 强制初始化。 */
    public static boolean initWithForName() throws ClassNotFoundException {
        LazyMarker.initialized = false;
        Class.forName(L00_ClassLoading.class.getName() + "$Lazy");
        return LazyMarker.initialized;
    }

    public static void runTests() {
        int c = touchCounter();
        if (c < 1) {
            throw new AssertionError("Counter 应已初始化");
        }
        if (Counter.hello() == null || !Counter.hello().startsWith("hi-")) {
            throw new AssertionError("hello 失败");
        }
        // 再次访问不应多次 ++（static 只跑一次）
        int before = Counter.initCount;
        Counter.hello();
        if (Counter.initCount != before) {
            throw new AssertionError("static 块不应重复执行");
        }
        if (!classNameOf().contains("Counter")) {
            throw new AssertionError("类名应含 Counter");
        }
        try {
            if (!"Counter".equals(forNameHello())) {
                throw new AssertionError("forName simpleName 应为 Counter");
            }
            if (!initWithForName()) {
                throw new AssertionError("forName 应触发 Lazy 初始化");
            }
        } catch (ClassNotFoundException e) {
            throw new AssertionError("不应 ClassNotFound", e);
        }
        System.out.println("✅ ClassLoading 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ClassLoading ************\n");
        System.out.println("class=" + classNameOf());
        System.out.println("hello=" + Counter.hello());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
