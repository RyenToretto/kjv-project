/**
 * [主题]: JVM 内存区域直觉 —— 栈帧 vs 堆对象
 * [难度]: Medium
 * [前置]: 00_java_basics、02_collections_deep
 *
 * 一、主题说明
 * 每个线程有自己的 JVM 栈：方法调用时压栈帧，局部变量/操作数在帧里。
 * 用 new 出来的对象在堆（heap）上；栈帧里的局部变量往往只是「引用」（指向堆）。
 * 方法返回后栈帧弹出，但堆对象要等没有引用才可被 GC。
 *
 * 二、写法对比
 * 1. 直观版：用图示注释 + 简单引用赋值演示「别名」。
 * 2. 进阶版：Runtime 读堆内存近似值；递归深度估算栈消耗（可控深度，避免真 StackOverflow）。
 *
 * 三、真实用途
 * - 解释「为什么改了方法参数里的对象字段，外面能看到」。
 * - OOM：堆不够 vs 栈溢出，排查方向不同。
 */
public class L00_MemoryModel {

    static class Box {
        int value;

        Box(int value) {
            this.value = value;
        }
    }

    /** 直观版：引用别名 —— 两个变量指向同一堆对象。 */
    public static int aliasDemo() {
        Box a = new Box(1);
        Box b = a; // 没有 new，b 与 a 同对象
        b.value = 99;
        return a.value; // 99
    }

    /** 值传递：重绑参数引用不影响调用方引用。 */
    public static void rebind(Box box) {
        box = new Box(0); // 只改了方法栈帧里的局部引用
    }

    /** 通过引用改堆上字段，调用方可见。 */
    public static void mutate(Box box) {
        box.value = 7;
    }

    /** 可控递归：返回实际递归到的深度（用于理解栈帧一层一层叠）。 */
    public static int depthProbe(int current, int limit) {
        if (current >= limit) {
            return current;
        }
        return depthProbe(current + 1, limit);
    }

    /** 堆内存快照（字节）：仅作观察，不同 JVM/时机数值会变。 */
    public static long usedHeapBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    public static void runTests() {
        if (aliasDemo() != 99) {
            throw new AssertionError("别名演示失败");
        }
        Box box = new Box(1);
        rebind(box);
        if (box.value != 1) {
            throw new AssertionError("rebind 不应改变调用方指向的对象字段");
        }
        mutate(box);
        if (box.value != 7) {
            throw new AssertionError("mutate 应改变堆上字段");
        }
        if (depthProbe(0, 100) != 100) {
            throw new AssertionError("递归深度应为 100");
        }
        if (usedHeapBytes() <= 0) {
            throw new AssertionError("已用堆应 > 0");
        }
        System.out.println("✅ MemoryModel 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 MemoryModel ************\n");
        System.out.println("aliasDemo → " + aliasDemo());
        System.out.println("usedHeapBytes ≈ " + usedHeapBytes());
        System.out.println("depthProbe(0,50) → " + depthProbe(0, 50));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
