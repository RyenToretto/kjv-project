import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * [主题]: Set 深挖 —— 去重与集合运算
 * [难度]: Easy～Medium
 * [前置]: 01_map
 *
 * 一、主题说明
 * Set：不重复集合。HashSet 无序；LinkedHashSet 保插入序；TreeSet 排序。
 * 依赖元素的 equals / hashCode（下一课细讲）。
 *
 * 二、写法对比
 * 1. 直观版：List 里手动 contains 去重。
 * 2. 进阶版：HashSet / 交集并集差集。
 *
 * 三、真实用途
 * - 权限码去重、已读消息 id、标签集合。
 * - 判断「是否已处理」比 List.contains 更合适（大数据量时）。
 */
public class L00_SetDeepDive {

    public static List<String> dedupeNaive(List<String> input) {
        List<String> out = new ArrayList<>();
        for (String s : input) {
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out;
    }

    public static List<String> dedupeBetter(List<String> input) {
        return new ArrayList<>(new LinkedHashSet<>(input));
    }

    public static Set<Integer> intersection(Set<Integer> a, Set<Integer> b) {
        Set<Integer> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
    }

    public static Set<Integer> union(Set<Integer> a, Set<Integer> b) {
        Set<Integer> r = new HashSet<>(a);
        r.addAll(b);
        return r;
    }

    public static Set<Integer> difference(Set<Integer> a, Set<Integer> b) {
        Set<Integer> r = new HashSet<>(a);
        r.removeAll(b);
        return r;
    }

    public static List<Integer> sortedUnique(int... nums) {
        Set<Integer> set = new TreeSet<>();
        for (int n : nums) {
            set.add(n);
        }
        return new ArrayList<>(set);
    }

    public static void runTests() {
        List<String> src = List.of("a", "b", "a", "c", "b");
        if (!dedupeNaive(src).equals(List.of("a", "b", "c"))) {
            throw new AssertionError("naive 去重失败");
        }
        if (!dedupeBetter(src).equals(List.of("a", "b", "c"))) {
            throw new AssertionError("better 去重失败");
        }
        Set<Integer> a = Set.of(1, 2, 3);
        Set<Integer> b = Set.of(2, 3, 4);
        if (!intersection(a, b).equals(Set.of(2, 3))) {
            throw new AssertionError("交集错误");
        }
        if (!union(a, b).equals(Set.of(1, 2, 3, 4))) {
            throw new AssertionError("并集错误");
        }
        if (!difference(a, b).equals(Set.of(1))) {
            throw new AssertionError("差集错误");
        }
        if (!sortedUnique(3, 1, 2, 1).equals(List.of(1, 2, 3))) {
            throw new AssertionError("TreeSet 排序去重失败");
        }
        System.out.println("✅ SetDeepDive 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SetDeepDive ************\n");
        System.out.println(dedupeBetter(List.of("x", "y", "x")));
        System.out.println(sortedUnique(5, 1, 5, 2));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
