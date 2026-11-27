import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [主题]: 健康检查与 K8s 探针 —— liveness / readiness / startup
 * [难度]: Medium
 * [前置]: 14 Spring 生产
 *
 * 一、主题说明
 * - liveness：挂了就重启（别把依赖失败算进来，防雪崩重启）
 * - readiness：能否接流量（依赖 DB 未就绪 → 503）
 * - startup：慢启动保护
 * Spring Actuator：/actuator/health。本课输出探针决策与一份迷你 Deployment 片段校验。
 *
 * 二、写法对比
 * 1. 直观版：一个 /health 干所有事。
 * 2. 进阶版：拆分三类探针逻辑。
 *
 * 三、真实用途
 * - K8s 滚动发布；滚动时 readiness 摘流。
 */
public class L00_HealthProbes {

    record AppState(boolean processOk, boolean dbOk, boolean warmedUp) {}

    public static int liveness(AppState s) {
        return s.processOk() ? 200 : 500;
    }

    public static int readiness(AppState s) {
        return s.processOk() && s.dbOk() && s.warmedUp() ? 200 : 503;
    }

    public static int startup(AppState s) {
        return s.warmedUp() ? 200 : 503;
    }

    public static Map<String, Object> actuatorStyle(AppState s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", readiness(s) == 200 ? "UP" : "DOWN");
        m.put("liveness", liveness(s) == 200 ? "UP" : "DOWN");
        m.put("db", s.dbOk() ? "UP" : "DOWN");
        return m;
    }

    /** 校验 YAML 片段是否包含必要探针字段（字符串级）。 */
    public static boolean validateProbeYaml(String yaml) {
        return yaml.contains("livenessProbe:")
                && yaml.contains("readinessProbe:")
                && yaml.contains("httpGet:")
                && yaml.contains("path:");
    }

    public static String sampleDeploymentProbeSnippet() {
        return """
                livenessProbe:
                  httpGet:
                    path: /actuator/health/liveness
                    port: 8080
                  initialDelaySeconds: 20
                readinessProbe:
                  httpGet:
                    path: /actuator/health/readiness
                    port: 8080
                  initialDelaySeconds: 5
                """;
    }

    public static void runTests() {
        AppState booting = new AppState(true, false, false);
        if (liveness(booting) != 200) {
            throw new AssertionError("进程活着 liveness 应 200");
        }
        if (readiness(booting) != 503 || startup(booting) != 503) {
            throw new AssertionError("未就绪应 503");
        }
        AppState ready = new AppState(true, true, true);
        if (readiness(ready) != 200) {
            throw new AssertionError("就绪应 200");
        }
        AppState dead = new AppState(false, true, true);
        if (liveness(dead) != 500) {
            throw new AssertionError("进程死 liveness 500");
        }
        if (!"UP".equals(actuatorStyle(ready).get("status"))) {
            throw new AssertionError("actuator UP");
        }
        if (!validateProbeYaml(sampleDeploymentProbeSnippet())) {
            throw new AssertionError("YAML 片段应含探针");
        }
        System.out.println("✅ HealthProbes 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 HealthProbes ************\n");
        System.out.println(actuatorStyle(new AppState(true, true, true)));
        System.out.println(sampleDeploymentProbeSnippet());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
