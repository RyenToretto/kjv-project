import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: 索引直觉 —— 全表扫描 vs 哈希/有序索引
 * [难度]: Medium
 * [前置]: 00_sql
 *
 * 一、主题说明
 * 没有索引：查找往往要扫很多行（全表扫描）。
 * B+Tree 索引（MySQL InnoDB 主键/二级索引）让范围与等值查找更快；本课用 Hash 模拟「等值索引」。
 * 索引不是越多越好：占空间、拖慢写入；要贴合查询条件。
 *
 * 二、写法对比
 * 1. 直观版：List 线性查找，统计比较次数。
 * 2. 进阶版：HashMap 二级索引，比较次数 ≈ 1。
 *
 * 三、真实用途
 * - EXPLAIN 看 type / key；慢查询优化第一刀常常是缺索引。
 * - 联合索引最左前缀（进阶面试点，本课先建立「按列建索引」直觉）。
 */
public class L00_IndexIntuition {

    public static class Row {
        final int id;
        final String email;

        Row(int id, String email) {
            this.id = id;
            this.email = email;
        }
    }

    public static class ScanStats {
        int comparisons;
        Row found;
    }

    /** 全表扫描按 email 找。 */
    public static ScanStats findByScan(List<Row> table, String email) {
        ScanStats s = new ScanStats();
        for (Row r : table) {
            s.comparisons++;
            if (r.email.equals(email)) {
                s.found = r;
                return s;
            }
        }
        return s;
    }

    /** 等值索引：email → row。 */
    public static ScanStats findByIndex(Map<String, Row> emailIndex, String email) {
        ScanStats s = new ScanStats();
        s.comparisons = 1; // 理想化：一次哈希定位
        s.found = emailIndex.get(email);
        return s;
    }

    public static Map<String, Row> buildEmailIndex(List<Row> table) {
        Map<String, Row> idx = new HashMap<>();
        for (Row r : table) {
            idx.put(r.email, r);
        }
        return idx;
    }

    public static List<Row> sample(int n) {
        List<Row> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Row(i, "u" + i + "@ex.com"));
        }
        return list;
    }

    public static void runTests() {
        List<Row> table = sample(1000);
        ScanStats scan = findByScan(table, "u999@ex.com");
        if (scan.found == null || scan.comparisons != 1000) {
            throw new AssertionError("扫到最后一行应比较 1000 次");
        }
        Map<String, Row> idx = buildEmailIndex(table);
        ScanStats indexed = findByIndex(idx, "u999@ex.com");
        if (indexed.found == null || indexed.found.id != 999) {
            throw new AssertionError("索引查找失败");
        }
        if (indexed.comparisons >= scan.comparisons) {
            throw new AssertionError("索引比较次数应远小于全表扫描");
        }
        if (findByIndex(idx, "nope").found != null) {
            throw new AssertionError("不存在应 null");
        }
        System.out.println("✅ IndexIntuition 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 IndexIntuition ************\n");
        List<Row> table = sample(200);
        ScanStats a = findByScan(table, "u150@ex.com");
        ScanStats b = findByIndex(buildEmailIndex(table), "u150@ex.com");
        System.out.println("scan comps=" + a.comparisons + ", index comps=" + b.comparisons);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
