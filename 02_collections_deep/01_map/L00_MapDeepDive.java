import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * [主题]: Map 深挖 —— HashMap / LinkedHashMap / TreeMap
 * [难度]: Medium
 * [前置]: 00_list
 *
 * 一、主题说明
 * Map：键值对，键唯一。HashMap 最常用（无序、平均 O(1)）。
 * LinkedHashMap：保持插入序；TreeMap：按键排序（需要 Comparable 或 Comparator）。
 *
 * 二、写法对比
 * 1. 直观版：两个 List「平行数组」模拟字典（易错位）。
 * 2. 进阶版：HashMap；getOrDefault / merge / computeIfAbsent。
 *
 * 三、真实用途
 * - 缓存 id→实体；统计词频；请求头、配置项。
 * - 面试：HashMap 扰动、树化等原理后续 JVM/源码阶段再挖，本课先会用。
 */
public class L00_MapDeepDive {

    /** 直观版：平行列表查值（演示为什么需要 Map）。 */
    public static String lookupNaive(String[] keys, String[] values, String key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                return values[i];
            }
        }
        return null;
    }

    public static Map<String, Integer> wordCount(String[] words) {
        Map<String, Integer> map = new HashMap<>();
        for (String w : words) {
            map.merge(w, 1, Integer::sum);
        }
        return map;
    }

    public static int getOrZero(Map<String, Integer> map, String key) {
        return map.getOrDefault(key, 0);
    }

    /** computeIfAbsent：没有则创建。 */
    public static Map<String, StringBuilder> groupLines(String[] lines) {
        Map<String, StringBuilder> map = new LinkedHashMap<>();
        for (String line : lines) {
            String key = line.isEmpty() ? "?" : line.substring(0, 1);
            map.computeIfAbsent(key, k -> new StringBuilder()).append(line).append(';');
        }
        return map;
    }

    public static Map<String, Integer> sortedByKey(Map<String, Integer> raw) {
        return new TreeMap<>(raw);
    }

    public static void runTests() {
        if (!"b".equals(lookupNaive(new String[]{"a", "b"}, new String[]{"1", "b"}, "b"))) {
            throw new AssertionError("naive lookup 失败");
        }
        Map<String, Integer> wc = wordCount(new String[]{"a", "b", "a", "a"});
        if (getOrZero(wc, "a") != 3 || getOrZero(wc, "b") != 1 || getOrZero(wc, "z") != 0) {
            throw new AssertionError("词频错误: " + wc);
        }
        Map<String, StringBuilder> grouped = groupLines(new String[]{"apple", "ant", "banana"});
        if (!grouped.get("a").toString().equals("apple;ant;")) {
            throw new AssertionError("分组失败: " + grouped.get("a"));
        }
        Map<String, Integer> sorted = sortedByKey(Map.of("b", 2, "a", 1));
        if (!sorted.keySet().iterator().next().equals("a")) {
            throw new AssertionError("TreeMap 应按键排序");
        }
        // put 覆盖
        Map<String, String> m = new HashMap<>();
        m.put("id", "1");
        m.put("id", "2");
        if (!"2".equals(m.get("id")) || m.size() != 1) {
            throw new AssertionError("同键覆盖失败");
        }
        System.out.println("✅ MapDeepDive 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 MapDeepDive ************\n");
        System.out.println(wordCount(new String[]{"java", "spring", "java"}));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
