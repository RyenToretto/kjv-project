import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * [主题]: Lambda 与函数式接口入门
 * [难度]: Easy～Medium
 * [前置]: 00_java_basics（尤其接口）
 *
 * 一、主题说明
 * Lambda 是「可传递的一小段行为」：把方法当参数用。
 * 函数式接口：只有一个抽象方法的接口（如 Predicate、Function、Comparator）。
 * 写法：(参数) -> 表达式  或  (参数) -> { 语句块; }
 *
 * 二、写法对比
 * 1. 直观版：匿名内部类实现 Comparator / Predicate。
 * 2. 进阶版：Lambda 与方法引用（String::length）。
 *
 * 三、真实用途
 * - 集合排序、过滤；Stream API 的基础。
 * - Spring 里大量 Callback；CompletableFuture 回调等。
 */
public class L00_LambdaBasics {

    /** 直观版：匿名类排序（按字符串长度升序）。 */
    public static List<String> sortByLengthNaive(List<String> input) {
        List<String> copy = new ArrayList<>(input);
        copy.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        });
        return copy;
    }

    /** 进阶版：Lambda。 */
    public static List<String> sortByLengthBetter(List<String> input) {
        List<String> copy = new ArrayList<>(input);
        copy.sort((a, b) -> Integer.compare(a.length(), b.length()));
        return copy;
    }

    /** 方法引用版：Comparator.comparingInt(String::length)。 */
    public static List<String> sortByLengthRef(List<String> input) {
        List<String> copy = new ArrayList<>(input);
        copy.sort(Comparator.comparingInt(String::length));
        return copy;
    }

    /** 用 Predicate 过滤：保留长度 >= min 的。 */
    public static List<String> filterBy(List<String> input, Predicate<String> rule) {
        List<String> out = new ArrayList<>();
        for (String s : input) {
            if (rule.test(s)) {
                out.add(s);
            }
        }
        return out;
    }

    /** Function：把每个元素映射成另一种值。 */
    public static List<Integer> mapLengths(List<String> input) {
        Function<String, Integer> toLen = String::length;
        List<Integer> out = new ArrayList<>();
        for (String s : input) {
            out.add(toLen.apply(s));
        }
        return out;
    }

    public static void runTests() {
        List<String> src = List.of("java", "go", "spring", "c");
        List<String> a = sortByLengthNaive(src);
        List<String> b = sortByLengthBetter(src);
        List<String> c = sortByLengthRef(src);
        if (!a.equals(b) || !b.equals(c)) {
            throw new AssertionError("三种排序结果应一致: " + a + " / " + b + " / " + c);
        }
        if (!List.of("c", "go", "java", "spring").equals(a)) {
            throw new AssertionError("排序顺序不对: " + a);
        }
        List<String> longOnes = filterBy(src, s -> s.length() >= 4);
        if (!longOnes.equals(List.of("java", "spring"))) {
            throw new AssertionError("过滤失败: " + longOnes);
        }
        if (!mapLengths(List.of("ab", "xyz")).equals(List.of(2, 3))) {
            throw new AssertionError("mapLengths 失败");
        }
        System.out.println("✅ LambdaBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 LambdaBasics ************\n");
        System.out.println(sortByLengthBetter(List.of("pear", "fig", "apple")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
