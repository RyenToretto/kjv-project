import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [主题]: Spring Boot 心智模型 —— 自动配置、起步依赖、内嵌容器（概念课）
 * [难度]: Easy～Medium
 * [前置]: 00_～05_；本课不要求已会 Maven/注解
 *
 * 一、主题说明
 * 传统 Java Web：自己拼 Servlet 容器 + Spring XML + 依赖版本，很劝退。
 * Spring Boot：约定优于配置 —— 加 starter 依赖，主类 @SpringBootApplication，常能直接跑 REST。
 * 本课用纯 JDK「假想清单」说明 Boot 启动时大致做了什么，避免小白被注解吓跑。
 *
 * 二、写法对比
 * 1. 直观版：手写「需要 Tomcat、需要 JSON、需要端口」清单。
 * 2. 进阶版：按 starter 名称解析出能力集合（模拟自动配置决策）。
 *
 * 三、真实用途
 * - 国内 Java 岗基本盘；后续真项目用 start.spring.io 生成再对照本课。
 */
public class L00_SpringBootMentalModel {

    /** 直观版：人工列出能力。 */
    public static Map<String, Boolean> manualStack(boolean needWeb, boolean needRedis) {
        Map<String, Boolean> m = new LinkedHashMap<>();
        m.put("embeddedTomcat", needWeb);
        m.put("jacksonJson", needWeb);
        m.put("dispatcherServlet", needWeb);
        m.put("redisTemplate", needRedis);
        return m;
    }

    /**
     * 进阶版：根据「starter」名字决定装配哪些能力。
     * 真实 Boot 用 @ConditionalOnClass 等；这里用字符串包含模拟。
     */
    public static Map<String, Boolean> fromStarters(String... starters) {
        boolean web = false;
        boolean redis = false;
        for (String s : starters) {
            String x = s.toLowerCase();
            if (x.contains("starter-web")) {
                web = true;
            }
            if (x.contains("starter-data-redis")) {
                redis = true;
            }
        }
        return manualStack(web, redis);
    }

    public static String explainMainClass() {
        return "public static void main → SpringApplication.run(App.class) "
                + "会创建 ApplicationContext、刷新 Bean、启动内嵌 Web 容器";
    }

    public static int defaultPort() {
        return 8080; // server.port 可改
    }

    public static void runTests() {
        Map<String, Boolean> webOnly = fromStarters("spring-boot-starter-web");
        if (!Boolean.TRUE.equals(webOnly.get("embeddedTomcat"))) {
            throw new AssertionError("web starter 应启用内嵌 Tomcat");
        }
        if (Boolean.TRUE.equals(webOnly.get("redisTemplate"))) {
            throw new AssertionError("未引入 redis starter 不应启用 redis");
        }
        Map<String, Boolean> both = fromStarters(
                "spring-boot-starter-web", "spring-boot-starter-data-redis");
        if (!Boolean.TRUE.equals(both.get("redisTemplate"))) {
            throw new AssertionError("redis starter 应启用 redisTemplate");
        }
        if (defaultPort() != 8080) {
            throw new AssertionError("默认端口 8080");
        }
        if (!explainMainClass().contains("SpringApplication.run")) {
            throw new AssertionError("说明文案应含 run");
        }
        System.out.println("✅ SpringBootMentalModel 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SpringBootMentalModel ************\n");
        System.out.println(explainMainClass());
        System.out.println(fromStarters("spring-boot-starter-web"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
