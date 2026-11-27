import java.util.Arrays;

/**
 * [主题]: 数组与字符串（String / StringBuilder）
 * [难度]: Easy
 * [前置]: 02_methods
 *
 * 一、主题说明
 * 数组：固定长度的同类型序列，下标从 0 开始。
 * String：不可变字符序列；拼接多段时优先 StringBuilder，避免反复创建临时对象。
 *
 * 二、写法对比
 * 1. 直观版：手动 for 找最大值；用 + 拼接字符串。
 * 2. 进阶版：Arrays.stream 求最大；StringBuilder / String.join 拼接。
 *
 * 三、真实用途
 * - 批量 ID、配置项列表；接口参数校验。
 * - 日志、SQL 片段、JSON 手工拼装（生产更常用模板或序列化库，但原理相同）。
 * - 注意：金额不要用 double 字符串糊弄，后续用 BigDecimal。
 */
public class L00_ArraysAndStrings {

    /** 直观版：遍历找最大值；空数组返回 Integer.MIN_VALUE 作为哨兵。 */
    public static int maxNaive(int[] nums) {
        if (nums == null || nums.length == 0) {
            return Integer.MIN_VALUE;
        }
        int max = nums[0];
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > max) {
                max = nums[i];
            }
        }
        return max;
    }

    /** 进阶版：Stream（后面 01_java8_plus 会系统讲；此处先见识）。 */
    public static int maxBetter(int[] nums) {
        if (nums == null || nums.length == 0) {
            return Integer.MIN_VALUE;
        }
        return Arrays.stream(nums).max().orElse(Integer.MIN_VALUE);
    }

    /** 直观版：+ 拼接（短文本可以，长循环里很浪费）。 */
    public static String joinNaive(String[] parts) {
        String result = "";
        for (int i = 0; i < parts.length; i++) {
            result = result + parts[i];
            if (i < parts.length - 1) {
                result = result + ",";
            }
        }
        return result;
    }

    /** 进阶版：String.join（JDK 自带，清晰且高效）。 */
    public static String joinBetter(String[] parts) {
        return String.join(",", parts);
    }

    /** StringBuilder：可变缓冲，适合循环追加。 */
    public static String repeatWithBuilder(String token, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(token);
        }
        return sb.toString();
    }

    /** 字符串常用操作演示：判空、包含、截取、大小写。 */
    public static boolean isValidUsername(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        // 长度 3～16，且只含小写字母数字（简化规则）
        if (trimmed.length() < 3 || trimmed.length() > 16) {
            return false;
        }
        return trimmed.equals(trimmed.toLowerCase())
                && trimmed.chars().allMatch(ch -> Character.isLetterOrDigit(ch));
    }

    public static void runTests() {
        int[] sample = {3, 9, 2, 9, 5};
        if (maxNaive(sample) != 9) {
            throw new AssertionError("maxNaive 应为 9");
        }
        if (maxBetter(sample) != 9) {
            throw new AssertionError("maxBetter 应为 9");
        }
        if (maxNaive(new int[]{}) != Integer.MIN_VALUE) {
            throw new AssertionError("空数组应返回 MIN_VALUE");
        }
        String[] parts = {"a", "b", "c"};
        if (!"a,b,c".equals(joinNaive(parts))) {
            throw new AssertionError("joinNaive 失败");
        }
        if (!"a,b,c".equals(joinBetter(parts))) {
            throw new AssertionError("joinBetter 失败");
        }
        if (!"ha".repeat(3).equals(repeatWithBuilder("ha", 3))) {
            throw new AssertionError("repeatWithBuilder 失败");
        }
        if (!isValidUsername("alice01")) {
            throw new AssertionError("alice01 应合法");
        }
        if (isValidUsername("Ab")) {
            throw new AssertionError("过短或含大写应不合法");
        }
        if (isValidUsername("  ")) {
            throw new AssertionError("空白应不合法");
        }
        System.out.println("✅ ArraysAndStrings 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ArraysAndStrings ************\n");
        int[] nums = {4, 1, 7, 3};
        System.out.println("max = " + maxNaive(nums));
        System.out.println("join = " + joinBetter(new String[]{"java", "spring", "redis"}));
        System.out.println("username ok? " + isValidUsername("bob123"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
