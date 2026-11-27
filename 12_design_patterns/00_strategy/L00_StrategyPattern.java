import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: 策略模式 —— 可替换算法（Spring 里随处可见）
 * [难度]: Medium
 * [前置]: 00 接口、06 DI
 *
 * 一、主题说明
 * 策略：把「变化的算法」封成接口，运行时选择实现，避免巨型 if-else。
 * Spring：注入 Map&lt;String, Strategy&gt; 或 @Autowired List。
 *
 * 二、写法对比
 * 1. 直观版：switch 运费。
 * 2. 进阶版：FreightStrategy 注册表。
 *
 * 三、真实用途
 * - 支付渠道、促销计价、认证方式。
 */
public class L00_StrategyPattern {

    interface FreightStrategy {
        int fee(int weightKg);
    }

    public static int feeNaive(String type, int weightKg) {
        return switch (type) {
            case "std" -> 500 + weightKg * 100;
            case "exp" -> 1200 + weightKg * 200;
            default -> throw new IllegalArgumentException(type);
        };
    }

    public static Map<String, FreightStrategy> strategies() {
        Map<String, FreightStrategy> m = new HashMap<>();
        m.put("std", w -> 500 + w * 100);
        m.put("exp", w -> 1200 + w * 200);
        return m;
    }

    public static int feeBetter(Map<String, FreightStrategy> map, String type, int weightKg) {
        FreightStrategy s = map.get(type);
        if (s == null) {
            throw new IllegalArgumentException(type);
        }
        return s.fee(weightKg);
    }

    public static void runTests() {
        Map<String, FreightStrategy> map = strategies();
        if (feeNaive("std", 2) != feeBetter(map, "std", 2)) {
            throw new AssertionError("std 应一致");
        }
        if (feeNaive("exp", 1) != feeBetter(map, "exp", 1)) {
            throw new AssertionError("exp 应一致");
        }
        boolean bad = false;
        try {
            feeBetter(map, "xx", 1);
        } catch (IllegalArgumentException ex) {
            bad = true;
        }
        if (!bad) {
            throw new AssertionError("未知策略应失败");
        }
        System.out.println("✅ StrategyPattern 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 StrategyPattern ************\n");
        System.out.println(feeBetter(strategies(), "exp", 3));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
