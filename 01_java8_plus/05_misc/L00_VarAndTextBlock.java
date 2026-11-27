import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [主题]: var 局部推断、文本块 Text Block、以及 switch 模式一点扩展
 * [难度]: Easy
 * [前置]: 01_stream、04_record（可选）
 *
 * 一、主题说明
 * var（Java 10+）：局部变量类型推断，右边必须能推出类型；不要滥用导致可读性下降。
 * 文本块（Java 15+）："""...""" 多行字符串，写 JSON/SQL/HTML 片段更干净。
 *
 * 二、写法对比
 * 1. 直观版：显式类型 + 传统转义字符串。
 * 2. 进阶版：var + 文本块；stripIndent 等。
 *
 * 三、真实用途
 * - 测试里塞 JSON 样例；简单 SQL 演示。
 * - 复杂泛型左边用 var 减少噪音（但公共 API 仍写清类型）。
 */
public class L00_VarAndTextBlock {

    /** 直观版：转义拼 JSON（易错）。 */
    public static String userJsonNaive(String name, int age) {
        return "{\"name\":\"" + name + "\",\"age\":" + age + "}";
    }

    /** 进阶版：文本块（注意：最终我们用 formatted 填值，仍是合法 JSON 单行或多行）。 */
    public static String userJsonBetter(String name, int age) {
        return """
                {"name":"%s","age":%d}
                """.formatted(name, age).trim();
    }

    /** var 演示：返回列表大小，内部用 var。 */
    public static int fillAndCount() {
        var list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        var it = list.iterator();
        int count = 0;
        while (it.hasNext()) {
            var item = it.next();
            if (!item.isBlank()) {
                count++;
            }
        }
        return count;
    }

    /** 多行 SQL 文本块：压缩空白后比较。 */
    public static String selectUserSql() {
        return """
                SELECT id, name
                FROM users
                WHERE active = 1
                """.stripIndent().trim();
    }

    public static void runTests() {
        if (!userJsonNaive("bob", 20).equals(userJsonBetter("bob", 20))) {
            throw new AssertionError("两种 JSON 应一致");
        }
        if (fillAndCount() != 2) {
            throw new AssertionError("fillAndCount 应为 2");
        }
        String sql = selectUserSql();
        if (!sql.contains("FROM users") || !sql.contains("active = 1")) {
            throw new AssertionError("SQL 文本块内容不对: " + sql);
        }
        // var 与钻石、Map.of 混用可读性检查（运行期只验证结果）
        var map = Map.of("k", 1);
        if (map.get("k") != 1) {
            throw new AssertionError("map 失败");
        }
        var copy = List.copyOf(List.of(1, 2, 3));
        if (copy.size() != 3) {
            throw new AssertionError("copy size 失败");
        }
        System.out.println("✅ VarAndTextBlock 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 VarAndTextBlock ************\n");
        System.out.println(userJsonBetter("alice", 18));
        System.out.println(selectUserSql());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
