import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * [主题]: JVM 诊断手册 —— jcmd / 线程 / 堆指标（可运行采集）
 * [难度]: Medium～Hard
 * [前置]: 03_troubleshooting
 *
 * 一、主题说明
 * 线上三板斧心智：看线程（死锁/卡顿）、看 GC/堆、看 CPU 热点。
 * 本课用 MXBean 采集可测指标，并生成「下一步该敲的 jcmd」清单（不强制本机执行 jcmd）。
 *
 * 二、写法对比
 * 1. 直观版：只看日志猜。
 * 2. 进阶版：指标快照 + 决策树输出命令。
 *
 * 三、真实用途
 * - 值班手册；对接 Arthas / async-profiler。
 */
public class L00_JvmDiagnosticsPlaybook {

    record Snapshot(long heapUsed, long heapMax, int threadCount, boolean deadlocked) {}

    public static Snapshot snapshot() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        long[] dead = threads.findDeadlockedThreads();
        return new Snapshot(
                mem.getHeapMemoryUsage().getUsed(),
                mem.getHeapMemoryUsage().getMax(),
                threads.getThreadCount(),
                dead != null && dead.length > 0);
    }

    public static List<String> suggestCommands(Snapshot s) {
        List<String> cmds = new ArrayList<>();
        long pid = ProcessHandle.current().pid();
        cmds.add("jcmd " + pid + " VM.version");
        double ratio = s.heapMax() <= 0 ? 0 : (double) s.heapUsed() / s.heapMax();
        if (ratio > 0.8) {
            cmds.add("jcmd " + pid + " GC.heap_info");
            cmds.add("jcmd " + pid + " GC.run");
        }
        if (s.deadlocked()) {
            cmds.add("jcmd " + pid + " Thread.print");
        }
        cmds.add("jcmd " + pid + " Thread.print");
        return cmds;
    }

    public static String classify(Snapshot s) {
        if (s.deadlocked()) {
            return "DEADLOCK";
        }
        double ratio = s.heapMax() <= 0 ? 0 : (double) s.heapUsed() / s.heapMax();
        if (ratio > 0.9) {
            return "HEAP_PRESSURE";
        }
        if (s.threadCount() > 500) {
            return "TOO_MANY_THREADS";
        }
        return "OK";
    }

    public static void runTests() {
        // 触发一点分配，避免个别 JVM 瞬间 used=0 的边界
        byte[] warm = new byte[256 * 1024];
        warm[0] = 1;
        Snapshot s = snapshot();
        if (s.heapUsed() < 0 || s.threadCount() <= 0 || s.heapMax() <= 0) {
            throw new AssertionError("快照异常: " + s);
        }
        List<String> cmds = suggestCommands(s);
        if (cmds.isEmpty() || !cmds.get(0).contains("VM.version")) {
            throw new AssertionError("命令清单不对: " + cmds);
        }
        String cls = classify(s);
        if (!List.of("OK", "HEAP_PRESSURE", "TOO_MANY_THREADS", "DEADLOCK").contains(cls)) {
            throw new AssertionError("未知分类: " + cls);
        }
        // 构造高压快照测分类
        Snapshot hot = new Snapshot(95, 100, 10, false);
        if (!"HEAP_PRESSURE".equals(classify(hot))) {
            throw new AssertionError("高压应 HEAP_PRESSURE");
        }
        Snapshot dl = new Snapshot(1, 100, 10, true);
        if (!"DEADLOCK".equals(classify(dl))) {
            throw new AssertionError("死锁分类");
        }
        System.out.println("✅ JvmDiagnosticsPlaybook 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 JvmDiagnosticsPlaybook ************\n");
        Snapshot s = snapshot();
        System.out.println(s);
        System.out.println("class=" + classify(s));
        System.out.println(suggestCommands(s));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
