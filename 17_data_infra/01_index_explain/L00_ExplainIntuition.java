import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [主题]: EXPLAIN 直觉 —— type / key / rows 估算
 * [难度]: Medium～Hard
 * [前置]: 05_index
 *
 * 一、主题说明
 * MySQL EXPLAIN 看执行计划：type（ALL/ref/range…）、possible_keys、key、rows。
 * 本课用有序列表模拟「二级索引等值 / 范围 / 全表」，输出迷你 explain 行。
 *
 * 二、写法对比
 * 1. 直观版：无序 List 全扫。
 * 2. 进阶版：按 email 排序后二分（模拟索引）。
 *
 * 三、真实用途
 * - 慢 SQL 优化第一工具；避免 SELECT * 大表无索引。
 */
public class L00_ExplainIntuition {

    record Row(int id, String email, int age) {}

    record Plan(String type, String key, int rowsExamined, Row found) {}

    public static Plan explainScan(List<Row> table, String email) {
        int examined = 0;
        for (Row r : table) {
            examined++;
            if (r.email().equals(email)) {
                return new Plan("ALL", null, examined, r);
            }
        }
        return new Plan("ALL", null, examined, null);
    }

    public static Plan explainIndexEq(List<Row> sortedByEmail, String email) {
        int lo = 0;
        int hi = sortedByEmail.size() - 1;
        int examined = 0;
        while (lo <= hi) {
            examined++;
            int mid = (lo + hi) >>> 1;
            Row r = sortedByEmail.get(mid);
            int cmp = r.email().compareTo(email);
            if (cmp == 0) {
                return new Plan("ref", "idx_email", examined, r);
            }
            if (cmp < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return new Plan("ref", "idx_email", examined, null);
    }

    public static Plan explainRangeAge(List<Row> sortedByAge, int minAge) {
        int examined = 0;
        Row first = null;
        for (Row r : sortedByAge) {
            if (r.age() >= minAge) {
                examined++;
                if (first == null) {
                    first = r;
                }
                // 模拟找到第一条满足范围后停止的「简化」——真实 range 会扫一段
                break;
            }
            examined++; // 跳过较小 age
        }
        return new Plan("range", "idx_age", examined, first);
    }

    public static List<Row> sample() {
        List<Row> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add(new Row(i, String.format("u%03d@ex.com", i), i % 50));
        }
        return list;
    }

    public static void runTests() {
        List<Row> table = sample();
        Plan scan = explainScan(table, "u150@ex.com");
        if (!"ALL".equals(scan.type()) || scan.rowsExamined() != 151) {
            throw new AssertionError("全表扫描计划不对: " + scan);
        }
        List<Row> byEmail = new ArrayList<>(table);
        byEmail.sort(Comparator.comparing(Row::email));
        Plan idx = explainIndexEq(byEmail, "u150@ex.com");
        if (!"ref".equals(idx.type()) || idx.rowsExamined() >= scan.rowsExamined()) {
            throw new AssertionError("索引等值应更少探测: " + idx);
        }
        if (idx.found() == null || idx.found().id() != 150) {
            throw new AssertionError("索引应找到 150");
        }
        List<Row> byAge = new ArrayList<>(table);
        byAge.sort(Comparator.comparingInt(Row::age));
        Plan range = explainRangeAge(byAge, 40);
        if (!"range".equals(range.type()) || range.found() == null || range.found().age() < 40) {
            throw new AssertionError("范围计划失败: " + range);
        }
        System.out.println("✅ ExplainIntuition 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ExplainIntuition ************\n");
        List<Row> table = sample();
        System.out.println(explainScan(table, "u010@ex.com"));
        List<Row> byEmail = new ArrayList<>(table);
        byEmail.sort(Comparator.comparing(Row::email));
        System.out.println(explainIndexEq(byEmail, "u010@ex.com"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
