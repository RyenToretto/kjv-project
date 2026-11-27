import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: 双亲委派 —— 自定义 ClassLoader 加载顺序
 * [难度]: Hard
 * [前置]: 03_jvm classloading
 *
 * 一、主题说明
 * 双亲委派：加载前先请父加载器尝试，保证核心类唯一、安全。
 * 打破委派：SPI、热部署、中间件（需理解 loadClass 重写点）。
 * 本课用「字符串字节码仓库」模拟父子加载器委派，不真的 defineClass（避免复杂），
 * 用「解析日志」证明委派顺序。
 *
 * 二、写法对比
 * 1. 直观版：自己 map 里有就加载（无委派）。
 * 2. 进阶版：先 parent.find，再自己。
 *
 * 三、真实用途
 * - Tomcat WebAppClassLoader；排查 NoClassDefFound / 类冲突。
 */
public class L00_ParentDelegation {

    static class FakeLoader {
        final String name;
        final FakeLoader parent;
        final Map<String, String> local = new HashMap<>();
        final StringBuilder log;

        FakeLoader(String name, FakeLoader parent, StringBuilder log) {
            this.name = name;
            this.parent = parent;
            this.log = log;
        }

        void defineLocal(String className, String bytecodeToken) {
            local.put(className, bytecodeToken);
        }

        /** 无委派：只查自己。 */
        String loadNaive(String className) {
            log.append(name).append(":self;");
            return local.get(className);
        }

        /** 双亲委派。 */
        String loadDelegating(String className) {
            if (parent != null) {
                log.append(name).append(">parent;");
                String fromParent = parent.loadDelegating(className);
                if (fromParent != null) {
                    return fromParent;
                }
            }
            log.append(name).append(":define;");
            return local.get(className);
        }
    }

    public static void runTests() {
        StringBuilder log = new StringBuilder();
        FakeLoader boot = new FakeLoader("boot", null, log);
        boot.defineLocal("java.lang.Object", "BOOT-OBJ");
        FakeLoader app = new FakeLoader("app", boot, log);
        app.defineLocal("com.demo.A", "APP-A");

        log.setLength(0);
        if (!"BOOT-OBJ".equals(app.loadDelegating("java.lang.Object"))) {
            throw new AssertionError("Object 应由 boot 提供");
        }
        if (!log.toString().contains("app>parent") || !log.toString().contains("boot:define")) {
            throw new AssertionError("委派日志不对: " + log);
        }

        log.setLength(0);
        if (!"APP-A".equals(app.loadDelegating("com.demo.A"))) {
            throw new AssertionError("应用类应由 app 定义");
        }
        // boot 找不到后 app define
        if (!log.toString().contains("boot:define") || !log.toString().contains("app:define")) {
            throw new AssertionError("应用类委派路径不对: " + log);
        }

        log.setLength(0);
        if (app.loadNaive("java.lang.Object") != null) {
            throw new AssertionError("naive 只查 app，不应有 Object");
        }
        System.out.println("✅ ParentDelegation 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ParentDelegation ************\n");
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
