import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: ORM 映射直觉 —— 表行 ↔ 对象
 * [难度]: Medium
 * [前置]: 05_mysql_jdbc、06_spring_boot
 *
 * 一、主题说明
 * ORM（对象关系映射）：用对象操作数据，框架帮你生成 SQL。
 * JPA/Hibernate：注解 @Entity；MyBatis：SQL 映射文件/注解，更贴近 SQL。
 * 本课手写「行 Map ↔ 实体」加深理解，避免把 JPA 当魔法。
 *
 * 二、写法对比
 * 1. 直观版：手写 getString 填字段。
 * 2. 进阶版：统一 Mapper 接口。
 *
 * 三、真实用途
 * - CRUD 业务主力；选型：快速 CRUD 偏 JPA，复杂 SQL/报表偏 MyBatis。
 */
public class L00_OrmMappingIdea {

    static class UserEntity {
        Long id;
        String name;
        String email;

        UserEntity() {}

        UserEntity(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    public static UserEntity fromRowNaive(Map<String, Object> row) {
        UserEntity u = new UserEntity();
        u.id = ((Number) row.get("id")).longValue();
        u.name = (String) row.get("name");
        u.email = (String) row.get("email");
        return u;
    }

    interface RowMapper<T> {
        T map(Map<String, Object> row);
    }

    public static final RowMapper<UserEntity> USER_MAPPER = row ->
            new UserEntity(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("email"));

    public static Map<String, Object> toRow(UserEntity u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.id);
        m.put("name", u.name);
        m.put("email", u.email);
        return m;
    }

    public static void runTests() {
        Map<String, Object> row = Map.of("id", 1, "name", "alice", "email", "a@x.com");
        UserEntity a = fromRowNaive(row);
        UserEntity b = USER_MAPPER.map(row);
        if (!a.name.equals(b.name) || !a.email.equals(b.email) || !a.id.equals(b.id)) {
            throw new AssertionError("两种映射应一致");
        }
        Map<String, Object> back = toRow(b);
        if (!back.get("email").equals("a@x.com")) {
            throw new AssertionError("写回行失败");
        }
        System.out.println("✅ OrmMappingIdea 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 OrmMappingIdea ************\n");
        UserEntity u = USER_MAPPER.map(Map.of("id", 9, "name", "bob", "email", "b@x.com"));
        System.out.println(u.id + " " + u.name);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
