/**
 * [主题]: JIT / 逃逸分析直觉 —— 热点编译与栈上分配可能
 * [难度]: Medium～Hard
 * [前置]: 03_memory
 *
 * 一、主题说明
 * 解释执行 → 热点方法 JIT 编译成机器码；逃逸分析可让「不逃逸对象」标量替换/栈分配（视 JVM）。
 * 本课用可测微基准对比：循环内创建短命对象 vs 复用缓冲（结果断言正确性，时间仅打印）。
 *
 * 二、写法对比
 * 1. 直观版：每次 new StringBuilder。
 * 2. 进阶版：复用 / 直接计算，减少分配。
 *
 * 三、真实用途
 * - 理解「为什么小对象不一定可怕」；优化前先度量。
 */
public class L00_JitAndEscapeIdea {

    public static long sumNaive(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += Integer.valueOf(i).longValue(); // 故意装箱
        }
        return sum;
    }

    public static long sumBetter(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    /** 不逃逸的短命对象：仅方法内使用。 */
    public static int localObjectHash(int x) {
        return Integer.valueOf(x).hashCode();
    }

    public static String buildNaive(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + i;
        }
        return s;
    }

    public static String buildBetter(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i);
        }
        return sb.toString();
    }

    public static void runTests() {
        if (sumNaive(100) != sumBetter(100)) {
            throw new AssertionError("两版求和应一致");
        }
        if (sumBetter(10) != 45) {
            throw new AssertionError("0..9 之和 45");
        }
        if (localObjectHash(7) != Integer.hashCode(7)) {
            throw new AssertionError("hash");
        }
        if (!buildNaive(5).equals(buildBetter(5))) {
            throw new AssertionError("拼接应一致");
        }
        long t1 = System.nanoTime();
        sumBetter(200_000);
        long betterNs = System.nanoTime() - t1;
        long t0 = System.nanoTime();
        sumNaive(200_000);
        long naiveNs = System.nanoTime() - t0;
        // 不强制谁更快（JIT 预热差异），只要求跑完
        if (betterNs <= 0 || naiveNs <= 0) {
            throw new AssertionError("计时应为正");
        }
        System.out.println("✅ JitAndEscapeIdea 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 JitAndEscapeIdea ************\n");
        System.out.println("sumBetter(100)=" + sumBetter(100));
        System.out.println("buildBetter(5)=" + buildBetter(5));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
