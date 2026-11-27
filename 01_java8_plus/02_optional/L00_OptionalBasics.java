import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * [主题]: Optional —— 显式表达「可能没有值」
 * [难度]: Easy～Medium
 * [前置]: 00_lambda
 *
 * 一、主题说明
 * Optional&lt;T&gt; 是容器：要么有值，要么 empty。用来减少无意义的 null 传递。
 * 不要：Optional 当字段乱飞、Optional.of(null)、在集合元素里塞 Optional。
 * 要：方法返回值可能缺失时用；用 map / flatMap / orElse / orElseThrow 链式处理。
 *
 * 二、写法对比
 * 1. 直观版：返回 null，调用方 if (x != null)。
 * 2. 进阶版：Optional + orElse / map。
 *
 * 三、真实用途
 * - Repository.findById 可能查无。
 * - 配置项缺失时的默认值；链式取嵌套属性避免 NPE。
 */
public class L00_OptionalBasics {

    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("u1", "alice");
        USERS.put("u2", "bob");
    }

    /** 直观版：找不到返回 null。 */
    public static String findNameNaive(String id) {
        return USERS.get(id); // 可能 null
    }

    /** 进阶版：Optional。 */
    public static Optional<String> findNameBetter(String id) {
        return Optional.ofNullable(USERS.get(id));
    }

    /** 显示名：有则大写，无则 guest。 */
    public static String displayName(String id) {
        return findNameBetter(id)
                .map(String::toUpperCase)
                .orElse("GUEST");
    }

    /** 必须存在，否则抛异常（业务上「按 id 必有」）。 */
    public static String requireName(String id) {
        return findNameBetter(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
    }

    /** flatMap：嵌套 Optional 展平（模拟再查邮箱）。 */
    public static Optional<String> findEmail(String id) {
        return findNameBetter(id).flatMap(name -> {
            if ("alice".equals(name)) {
                return Optional.of("alice@example.com");
            }
            return Optional.empty();
        });
    }

    public static void runTests() {
        if (!"alice".equals(findNameNaive("u1"))) {
            throw new AssertionError("naive u1 应为 alice");
        }
        if (findNameNaive("nope") != null) {
            throw new AssertionError("naive 不存在应为 null");
        }
        if (findNameBetter("u1").isEmpty() || !"alice".equals(findNameBetter("u1").get())) {
            throw new AssertionError("better u1 失败");
        }
        if (findNameBetter("nope").isPresent()) {
            throw new AssertionError("better 不存在应 empty");
        }
        if (!"ALICE".equals(displayName("u1"))) {
            throw new AssertionError("displayName u1 应为 ALICE");
        }
        if (!"GUEST".equals(displayName("nope"))) {
            throw new AssertionError("displayName 缺失应为 GUEST");
        }
        if (!"alice@example.com".equals(findEmail("u1").orElse(""))) {
            throw new AssertionError("alice 邮箱不对");
        }
        if (findEmail("u2").isPresent()) {
            throw new AssertionError("bob 无邮箱应 empty");
        }
        boolean thrown = false;
        try {
            requireName("nope");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        if (!thrown) {
            throw new AssertionError("requireName 缺失应抛异常");
        }
        System.out.println("✅ OptionalBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 OptionalBasics ************\n");
        System.out.println("u1 → " + displayName("u1"));
        System.out.println("xx → " + displayName("xx"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
