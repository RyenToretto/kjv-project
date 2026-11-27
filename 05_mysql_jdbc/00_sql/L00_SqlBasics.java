import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * [主题]: SQL 基础心智 —— SELECT / WHERE / INSERT 语义（内存表模拟）
 * [难度]: Easy～Medium
 * [前置]: 00_java_basics、02_collections_deep
 *
 * 一、主题说明
 * SQL 是声明式：你描述「要什么数据」，数据库决定「怎么找」。
 * 本课用内存表模拟，不连真 MySQL，但语句语义与真实库一致，便于零基础建立感觉。
 * 真实环境请在本机装 MySQL 8+ 对照练习（见阶段 README）。
 *
 * 二、写法对比
 * 1. 直观版：Java for 循环过滤「像 WHERE」。
 * 2. 进阶版：解析极简 SQL 子集（SELECT ... FROM t WHERE col = ?）。
 *
 * 三、真实用途
 * - 所有业务查询、报表、对账；ORM 最终也多半生成 SQL。
 */
public class L00_SqlBasics {

    /** 极简内存表：列名 → 值。 */
    public static class MiniTable {
        final String name;
        final List<Map<String, String>> rows = new ArrayList<>();

        MiniTable(String name) {
            this.name = name;
        }

        void insert(Map<String, String> row) {
            rows.add(new LinkedHashMap<>(row));
        }
    }

    public static List<Map<String, String>> whereEqualsNaive(
            MiniTable table, String column, String value) {
        List<Map<String, String>> out = new ArrayList<>();
        for (Map<String, String> row : table.rows) {
            if (value.equals(row.get(column))) {
                out.add(row);
            }
        }
        return out;
    }

    /**
     * 极简 SQL：仅支持
     * SELECT * FROM users WHERE status = 'ACTIVE'
     * （教学用，非完整解析器）
     */
    public static List<Map<String, String>> execSelect(MiniTable table, String sql) {
        String normalized = sql.trim().replaceAll("\\s+", " ");
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT * FROM ")) {
            throw new IllegalArgumentException("仅支持 SELECT * FROM ...");
        }
        int whereIdx = upper.indexOf(" WHERE ");
        String fromPart = whereIdx < 0
                ? normalized.substring("SELECT * FROM ".length())
                : normalized.substring("SELECT * FROM ".length(), whereIdx);
        if (!table.name.equalsIgnoreCase(fromPart.trim())) {
            throw new IllegalArgumentException("表名不匹配: " + fromPart);
        }
        if (whereIdx < 0) {
            return new ArrayList<>(table.rows);
        }
        String where = normalized.substring(whereIdx + " WHERE ".length()).trim();
        // col = 'value'
        String[] parts = where.split("=", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("WHERE 仅支持 col = 'value'");
        }
        String col = parts[0].trim();
        String raw = parts[1].trim();
        if (raw.length() < 2 || raw.charAt(0) != '\'' || raw.charAt(raw.length() - 1) != '\'') {
            throw new IllegalArgumentException("值请用单引号");
        }
        String val = raw.substring(1, raw.length() - 1);
        return whereEqualsNaive(table, col, val);
    }

    public static MiniTable sampleUsers() {
        MiniTable t = new MiniTable("users");
        t.insert(Map.of("id", "1", "name", "alice", "status", "ACTIVE"));
        t.insert(Map.of("id", "2", "name", "bob", "status", "DISABLED"));
        t.insert(Map.of("id", "3", "name", "cara", "status", "ACTIVE"));
        return t;
    }

    public static void runTests() {
        MiniTable users = sampleUsers();
        List<Map<String, String>> active = whereEqualsNaive(users, "status", "ACTIVE");
        if (active.size() != 2) {
            throw new AssertionError("ACTIVE 应有 2 人");
        }
        List<Map<String, String>> viaSql = execSelect(users,
                "SELECT * FROM users WHERE status = 'ACTIVE'");
        if (viaSql.size() != 2) {
            throw new AssertionError("SQL 过滤应有 2 行");
        }
        if (execSelect(users, "SELECT * FROM users").size() != 3) {
            throw new AssertionError("无 WHERE 应 3 行");
        }
        boolean bad = false;
        try {
            execSelect(users, "DELETE FROM users");
        } catch (IllegalArgumentException ex) {
            bad = true;
        }
        if (!bad) {
            throw new AssertionError("不支持的 SQL 应失败");
        }
        System.out.println("✅ SqlBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SqlBasics ************\n");
        System.out.println(execSelect(sampleUsers(), "SELECT * FROM users WHERE status = 'ACTIVE'"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
