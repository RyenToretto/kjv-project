/**
 * [主题]: 常见排查模式 —— StackOverflow、堆占用、工具入口心智
 * [难度]: Medium
 * [前置]: 00_memory、01_gc
 *
 * 一、主题说明
 * StackOverflowError：递归过深 / 超大栈帧。
 * OutOfMemoryError: Java heap space：堆对象过多或泄漏。
 * 本课用「可控深度」捕获 StackOverflow；用估算演示堆占用，不故意把机器打满。
 * 工具心智：jcmd / jmap / MAT / Arthas —— 课里只记「何时用」，不绑外部环境。
 *
 * 二、写法对比
 * 1. 直观版：无限递归（危险）—— 改为有上限尝试。
 * 2. 进阶版：捕获 Error 中的 StackOverflowError；诊断字符串模板。
 *
 * 三、真实用途
 * - 线上 CPU 高、Full GC、假死时的第一反应路径。
 * - 先看现象分类，再上工具，避免盲改代码。
 */
public class L00_CommonOomPatterns {

    /** 故意递归到溢出，返回是否捕获到 StackOverflowError。 */
    public static boolean catchStackOverflow() {
        try {
            infinite(0);
            return false;
        } catch (StackOverflowError error) {
            return true;
        }
    }

    private static int infinite(int n) {
        // 不优化掉递归：用结果依赖
        return infinite(n + 1) + n;
    }

    /** 安全递归：限制深度，不应溢出。 */
    public static int safeDepth(int n, int limit) {
        if (n >= limit) {
            return n;
        }
        return safeDepth(n + 1, limit);
    }

    /** 估算：n 个 byte[size] 的名义占用（不含对象头等）。 */
    public static long estimateBytes(int n, int size) {
        return (long) n * size;
    }

    /** 根据异常类型给排查建议。 */
    public static String diagnose(Throwable t) {
        if (t instanceof StackOverflowError) {
            return "检查递归/循环调用；缩小局部大数组；-Xss 仅作权宜";
        }
        if (t instanceof OutOfMemoryError) {
            String msg = String.valueOf(t.getMessage());
            if (msg.contains("heap")) {
                return "查堆泄漏与峰值；jcmd GC.heap_info / 堆转储 MAT";
            }
            return "查 OutOfMemory 子类型（metaspace/direct…）";
        }
        return "先看完整栈与日志时间线";
    }

    public static void runTests() {
        if (!catchStackOverflow()) {
            throw new AssertionError("应捕获 StackOverflowError");
        }
        if (safeDepth(0, 1000) != 1000) {
            throw new AssertionError("安全递归失败");
        }
        if (estimateBytes(1024, 1024) != 1024L * 1024) {
            throw new AssertionError("估算错误");
        }
        if (!diagnose(new StackOverflowError()).contains("递归")) {
            throw new AssertionError("SO 诊断文案不对");
        }
        if (!diagnose(new OutOfMemoryError("Java heap space")).contains("堆")) {
            throw new AssertionError("OOM 诊断文案不对");
        }
        System.out.println("✅ CommonOomPatterns 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CommonOomPatterns ************\n");
        System.out.println("catch SO? " + catchStackOverflow());
        System.out.println(diagnose(new OutOfMemoryError("Java heap space")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
