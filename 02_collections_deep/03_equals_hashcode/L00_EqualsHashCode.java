import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * [主题]: equals 与 hashCode —— Set/Map 正确性的根基
 * [难度]: Medium
 * [前置]: 02_set、01_map
 *
 * 一、主题说明
 * 默认 equals 是引用相等（==）。若业务上「字段相同即同一实体」，必须重写 equals。
 * 契约：equals 相等 ⇒ hashCode 必须相等；重写 equals 必须重写 hashCode。
 * 否则 HashSet / HashMap 会「明明相等却当两个键」。
 *
 * 二、写法对比
 * 1. 直观版：不重写，只比引用。
 * 2. 进阶版：按业务字段重写；或用 record 自动生成。
 *
 * 三、真实用途
 * - 实体去重、缓存键、JPA 实体相等性（生产更复杂，本课打基础）。
 */

/** 错误示范：只重写 equals 不重写 hashCode。 */
class BrokenUser {
    final String id;

    BrokenUser(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokenUser that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }
    // 故意不写 hashCode
}

/** 正确：equals + hashCode 成对。 */
class User {
    private final String id;
    private final String name;

    User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User(" + id + "," + name + ")";
    }
}

record UserKey(String id) {}

public class L00_EqualsHashCode {

    public static boolean brokenSetContainsDuplicateLogic() {
        Set<BrokenUser> set = new HashSet<>();
        set.add(new BrokenUser("u1"));
        // 可能仍能 add 成功第二个「业务相等」对象，因为 hash 桶不同
        return set.add(new BrokenUser("u1"));
    }

    public static int uniqueUsers(User... users) {
        Set<User> set = new HashSet<>();
        for (User u : users) {
            set.add(u);
        }
        return set.size();
    }

    public static void runTests() {
        User a = new User("1", "alice");
        User b = new User("1", "Alice-改名");
        User c = new User("2", "bob");
        if (!a.equals(b)) {
            throw new AssertionError("同 id 应 equals");
        }
        if (a.hashCode() != b.hashCode()) {
            throw new AssertionError("equals 则 hashCode 必须相同");
        }
        if (a.equals(c)) {
            throw new AssertionError("不同 id 不应 equals");
        }
        if (uniqueUsers(a, b, c) != 2) {
            throw new AssertionError("Set 去重后应 2 人");
        }
        // Broken：常常能塞进两个（依赖 hash 默认实现）
        if (!brokenSetContainsDuplicateLogic()) {
            // 极少数情况下桶碰撞可能导致 add 返回 false；再验证 size
            Set<BrokenUser> set = new HashSet<>();
            set.add(new BrokenUser("x"));
            set.add(new BrokenUser("x"));
            if (set.size() == 1) {
                throw new AssertionError("BrokenUser 预期常无法正确去重；若 size=1 属偶然，改用断言 hash 不一致场景");
            }
        }
        Set<UserKey> keys = new HashSet<>();
        keys.add(new UserKey("k1"));
        keys.add(new UserKey("k1"));
        if (keys.size() != 1) {
            throw new AssertionError("record 应自动正确去重");
        }
        System.out.println("✅ EqualsHashCode 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 EqualsHashCode ************\n");
        System.out.println("unique = " + uniqueUsers(new User("1", "a"), new User("1", "b"), new User("2", "c")));
        System.out.println("broken add duplicate? " + brokenSetContainsDuplicateLogic());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
