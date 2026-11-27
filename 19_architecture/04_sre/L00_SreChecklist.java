import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: SRE / 可观测性验收清单 —— SLI/SLO、告警、Runbook
 * [难度]: Medium～Hard
 * [前置]: 18_otel、18_health
 *
 * 一、主题说明
 * 精通不只写功能：要定义服务水平、告警、故障手册。
 * 本课用可运行检查器：对「服务元数据」打分，缺项列出。
 *
 * 二、写法对比
 * 1. 直观版：上线后靠用户投诉。
 * 2. 进阶版：上线门禁清单（可测）。
 *
 * 三、真实用途
 * - 发布评审；Oncall 交接；错误预算。
 */
public class L00_SreChecklist {

    record ServiceMeta(
            String name,
            boolean hasMetrics,
            boolean hasTracing,
            boolean hasLiveness,
            boolean hasReadiness,
            Double sloAvailability, // 0~1
            boolean hasRunbook,
            boolean hasPager
    ) {}

    public static List<String> gaps(ServiceMeta s) {
        List<String> g = new ArrayList<>();
        if (blank(s.name())) {
            g.add("缺少服务名");
        }
        if (!s.hasMetrics()) {
            g.add("缺少指标（QPS/延迟/错误率）");
        }
        if (!s.hasTracing()) {
            g.add("缺少链路追踪");
        }
        if (!s.hasLiveness() || !s.hasReadiness()) {
            g.add("缺少 K8s 探针");
        }
        if (s.sloAvailability() == null || s.sloAvailability() <= 0 || s.sloAvailability() > 1) {
            g.add("SLO 可用性未定义");
        }
        if (!s.hasRunbook()) {
            g.add("缺少 Runbook");
        }
        if (!s.hasPager()) {
            g.add("缺少值班/告警路由");
        }
        return g;
    }

    public static int score(ServiceMeta s) {
        int total = 7;
        return total - gaps(s).size();
    }

    public static boolean passGate(ServiceMeta s) {
        return gaps(s).isEmpty();
    }

    public static Map<String, Object> report(ServiceMeta s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("service", s.name());
        m.put("score", score(s));
        m.put("pass", passGate(s));
        m.put("gaps", gaps(s));
        if (s.sloAvailability() != null) {
            m.put("errorBudgetHint", "月可用性目标 " + s.sloAvailability());
        }
        return m;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    public static void runTests() {
        ServiceMeta bad = new ServiceMeta("demo", false, false, true, false, null, false, false);
        if (gaps(bad).size() < 5) {
            throw new AssertionError("应有多项缺口: " + gaps(bad));
        }
        if (passGate(bad)) {
            throw new AssertionError("不应过门禁");
        }
        ServiceMeta good = new ServiceMeta("order-api", true, true, true, true, 0.999, true, true);
        if (!passGate(good) || score(good) != 7) {
            throw new AssertionError("完整服务应满分过门禁");
        }
        Map<String, Object> r = report(good);
        if (!Boolean.TRUE.equals(r.get("pass"))) {
            throw new AssertionError("report.pass");
        }
        System.out.println("✅ SreChecklist 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SreChecklist ************\n");
        System.out.println(report(new ServiceMeta("order-api", true, true, true, true, 0.999, true, true)));
        System.out.println(report(new ServiceMeta("legacy", false, false, false, false, null, false, false)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
