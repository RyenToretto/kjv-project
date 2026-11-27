import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * [主题]: Stream 基础 —— filter / map / collect / 分组
 * [难度]: Medium
 * [前置]: 00_lambda
 *
 * 一、主题说明
 * Stream 是对数据序列的「声明式流水线」：描述要什么，而不是手写每一步循环。
 * 常见链：source → intermediate(filter/map/…) → terminal(collect/sum/…)
 * 注意：Stream 只能消费一次；默认不修改源集合。
 *
 * 二、写法对比
 * 1. 直观版：for 循环过滤 + 映射。
 * 2. 进阶版：stream().filter().map().toList() / Collectors.groupingBy。
 *
 * 三、真实用途
 * - 订单列表按状态过滤、按用户分组汇总金额。
 * - 接口返回前 DTO 映射；统计报表。
 */
public class L00_StreamBasics {

    /** 直观版：偶数平方组成新列表。 */
    public static List<Integer> evenSquaresNaive(List<Integer> nums) {
        List<Integer> out = new ArrayList<>();
        for (Integer n : nums) {
            if (n % 2 == 0) {
                out.add(n * n);
            }
        }
        return out;
    }

    /** 进阶版：Stream。toList() 为 Java 16+ 不可变列表。 */
    public static List<Integer> evenSquaresBetter(List<Integer> nums) {
        return nums.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .toList();
    }

    /** 按首字母分组（保持插入顺序用 LinkedHashMap）。 */
    public static Map<Character, List<String>> groupByInitial(List<String> words) {
        return words.stream()
                .collect(Collectors.groupingBy(
                        w -> Character.toLowerCase(w.charAt(0)),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /** 求和：mapToInt 避免装箱。 */
    public static int sumPrices(List<Integer> pricesCents) {
        return pricesCents.stream().mapToInt(Integer::intValue).sum();
    }

    /** anyMatch / allMatch 短小判断。 */
    public static boolean anyLongerThan(List<String> words, int len) {
        return words.stream().anyMatch(w -> w.length() > len);
    }

    public static void runTests() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);
        List<Integer> naive = evenSquaresNaive(nums);
        List<Integer> better = evenSquaresBetter(nums);
        if (!naive.equals(List.of(4, 16, 36))) {
            throw new AssertionError("直观版偶数平方错误: " + naive);
        }
        if (!naive.equals(better)) {
            throw new AssertionError("Stream 版应与直观版一致");
        }
        Map<Character, List<String>> groups = groupByInitial(List.of("Apple", "ant", "Banana", "berry"));
        if (!groups.get('a').equals(List.of("Apple", "ant"))) {
            throw new AssertionError("a 组错误: " + groups.get('a'));
        }
        if (sumPrices(List.of(100, 250, 50)) != 400) {
            throw new AssertionError("求和应为 400");
        }
        if (!anyLongerThan(List.of("a", "abcd"), 3)) {
            throw new AssertionError("应存在长度>3");
        }
        if (anyLongerThan(List.of("a", "bb"), 3)) {
            throw new AssertionError("不应存在长度>3");
        }
        System.out.println("✅ StreamBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 StreamBasics ************\n");
        System.out.println(evenSquaresBetter(List.of(1, 2, 3, 4)));
        System.out.println(groupByInitial(List.of("java", "junit", "spring")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
