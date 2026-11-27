/**
 * [主题]: GC 收集器对比心智 —— 吞吐 vs 延迟（G1 / ZGC）
 * [难度]: Medium～Hard
 * [前置]: 03_gc
 *
 * 一、主题说明
 * 选择收集器看 SLA：吞吐量优先还是尾延迟优先。
 * - G1：分区，目标停顿可设（-XX:MaxGCPauseMillis）
 * - ZGC / Shenandoah：超低停顿，适合大堆低延迟
 * 本课不启动真实 GC 参数实验（环境相关），用「决策表 + 可测评分函数」固化选型。
 *
 * 二、写法对比
 * 1. 直观版：一律 Parallel/默认。
 * 2. 进阶版：按 heapGB、p99Ms、throughput 打分选型。
 *
 * 三、真实用途
 * - 容量规划与 JVM 启动参数；结合 GC 日志验证。
 */
public class L00_GcCollectorCompare {

    enum Collector { PARALLEL, G1, ZGC }

    record Workload(double heapGb, double targetP99Ms, boolean preferThroughput) {}

    /** 直观版：永远 G1。 */
    public static Collector chooseNaive(Workload w) {
        return Collector.G1;
    }

    /**
     * 进阶版启发式（教学用，非官方算法）：
     * - 强吞吐且堆不大 → Parallel
     * - 要低延迟且堆很大 → ZGC
     * - 其它 → G1
     */
    public static Collector chooseBetter(Workload w) {
        if (w.preferThroughput() && w.heapGb() <= 4 && w.targetP99Ms() >= 50) {
            return Collector.PARALLEL;
        }
        if (w.targetP99Ms() <= 10 && w.heapGb() >= 8) {
            return Collector.ZGC;
        }
        return Collector.G1;
    }

    public static String flagsOf(Collector c) {
        return switch (c) {
            case PARALLEL -> "-XX:+UseParallelGC";
            case G1 -> "-XX:+UseG1GC -XX:MaxGCPauseMillis=200";
            case ZGC -> "-XX:+UseZGC";
        };
    }

    public static void runTests() {
        Workload batch = new Workload(2, 100, true);
        if (chooseBetter(batch) != Collector.PARALLEL) {
            throw new AssertionError("批处理吞吐应 Parallel");
        }
        Workload api = new Workload(16, 5, false);
        if (chooseBetter(api) != Collector.ZGC) {
            throw new AssertionError("大堆低延迟应 ZGC");
        }
        Workload general = new Workload(6, 30, false);
        if (chooseBetter(general) != Collector.G1) {
            throw new AssertionError("通用应 G1");
        }
        if (chooseNaive(api) != Collector.G1) {
            throw new AssertionError("naive 永远 G1");
        }
        if (!flagsOf(Collector.ZGC).contains("UseZGC")) {
            throw new AssertionError("ZGC 参数");
        }
        System.out.println("✅ GcCollectorCompare 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 GcCollectorCompare ************\n");
        Workload w = new Workload(12, 8, false);
        Collector c = chooseBetter(w);
        System.out.println(c + " → " + flagsOf(c));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
