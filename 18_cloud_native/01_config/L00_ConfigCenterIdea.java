import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * [主题]: 配置中心心智 —— 本地默认 / 远程覆盖 / 动态刷新监听
 * [难度]: Medium
 * [前置]: 06 Spring Boot
 *
 * 一、主题说明
 * Spring Cloud Config / Nacos：远端配置覆盖 application.yml；可监听变更刷新 @RefreshScope。
 * 本课实现多层 PropertySource + 监听器。
 *
 * 二、写法对比
 * 1. 直观版：写死常量。
 * 2. 进阶版：default ← file ← remote，后者优先。
 *
 * 三、真实用途
 * - 多环境配置；开关；动态调超时。
 */
public class L00_ConfigCenterIdea {

    static class Environment {
        private final Map<String, String> defaults = new HashMap<>();
        private final Map<String, String> remote = new HashMap<>();
        private final CopyOnWriteArrayList<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

        void setDefault(String k, String v) {
            defaults.put(k, v);
        }

        void publishRemote(String k, String v) {
            remote.put(k, v);
            for (Consumer<String> l : listeners) {
                l.accept(k);
            }
        }

        String get(String k) {
            if (remote.containsKey(k)) {
                return remote.get(k);
            }
            return defaults.get(k);
        }

        void onChange(Consumer<String> listener) {
            listeners.add(listener);
        }
    }

    public static void runTests() {
        Environment env = new Environment();
        env.setDefault("timeout.ms", "1000");
        env.setDefault("feature.x", "false");
        if (!"1000".equals(env.get("timeout.ms"))) {
            throw new AssertionError("默认值");
        }
        String[] last = {null};
        env.onChange(k -> last[0] = k);
        env.publishRemote("timeout.ms", "3000");
        if (!"3000".equals(env.get("timeout.ms"))) {
            throw new AssertionError("远程应覆盖");
        }
        if (!"timeout.ms".equals(last[0])) {
            throw new AssertionError("应触发监听");
        }
        if (!"false".equals(env.get("feature.x"))) {
            throw new AssertionError("未覆盖的保持默认");
        }
        System.out.println("✅ ConfigCenterIdea 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ConfigCenterIdea ************\n");
        Environment env = new Environment();
        env.setDefault("app.name", "kjv");
        env.publishRemote("app.name", "kjv-prod");
        System.out.println(env.get("app.name"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
