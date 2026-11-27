/**
 * [主题]: 控制流 —— if / switch / for / while
 * [难度]: Easy
 * [前置]: 00_syntax/L00_HelloAndTypes
 *
 * 一、主题说明
 * 程序不是永远从上往下直线执行。控制流决定「什么条件下做什么、重复多少次」。
 * 本课覆盖：条件分支（if / else、switch）、循环（for / while）。
 *
 * 二、写法对比
 * 1. 直观版：经典 if-else 与 for 计数循环，好读。
 * 2. 进阶版：switch 表达式（Java 14+，本仓库按 25）与增强 for；可返回值、不必 break。
 *
 * 三、真实用途
 * - 订单状态机：按状态分支处理支付 / 发货 / 取消。
 * - 批处理：遍历列表做校验、汇总金额。
 * - HTTP 状态码、错误码映射常用 switch。
 */
public class L00_ControlFlow {

    /** 直观版：用 if-else 判断成绩等级。 */
    public static String gradeNaive(int score) {
        // score 约定 0～100
        if (score >= 90) {
            return "A";
        } else if (score >= 80) {
            return "B";
        } else if (score >= 60) {
            return "C";
        } else {
            return "D";
        }
    }

    /**
     * 进阶版：switch 表达式，直接作为返回值。
     * 按「十位档」映射，演示箭头语法与 yield 均可；这里用箭头返回。
     */
    public static String gradeBetter(int score) {
        int band = score / 10; // 90～100 → 9 或 10
        return switch (band) {
            case 10, 9 -> "A";
            case 8 -> "B";
            case 7, 6 -> "C";
            default -> "D";
        };
    }

    /** 直观版：for 循环求 1..n 的和。 */
    public static int sumToNaive(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    /** 进阶版：等差数列公式（循环的对照，生产里简单汇总仍常用循环）。 */
    public static int sumToBetter(int n) {
        return n * (n + 1) / 2;
    }

    /** while：倒计数直到 0，返回拼接字符串，便于自测。 */
    public static String countdown(int from) {
        StringBuilder sb = new StringBuilder();
        int i = from;
        while (i > 0) {
            sb.append(i);
            if (i > 1) {
                sb.append(",");
            }
            i--;
        }
        return sb.toString();
    }

    /** 增强 for：遍历数组求和。 */
    public static int sumArray(int[] nums) {
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        return sum;
    }

    public static void runTests() {
        if (!"A".equals(gradeNaive(95))) {
            throw new AssertionError("95 应为 A");
        }
        if (!"C".equals(gradeNaive(60))) {
            throw new AssertionError("60 应为 C");
        }
        if (!"D".equals(gradeNaive(59))) {
            throw new AssertionError("59 应为 D");
        }
        if (!gradeNaive(88).equals(gradeBetter(88))) {
            throw new AssertionError("直观版与进阶版对 88 应一致");
        }
        if (!gradeNaive(100).equals(gradeBetter(100))) {
            throw new AssertionError("100 两版均应为 A");
        }
        if (sumToNaive(10) != 55) {
            throw new AssertionError("1..10 之和应为 55");
        }
        if (sumToBetter(10) != 55) {
            throw new AssertionError("公式版 1..10 应为 55");
        }
        if (!"3,2,1".equals(countdown(3))) {
            throw new AssertionError("countdown(3) 应为 3,2,1");
        }
        if (sumArray(new int[]{1, 2, 3, 4}) != 10) {
            throw new AssertionError("数组求和应为 10");
        }
        System.out.println("✅ ControlFlow 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ControlFlow ************\n");
        System.out.println("成绩 92 → " + gradeNaive(92));
        System.out.println("switch 成绩 75 → " + gradeBetter(75));
        System.out.println("1..5 之和 = " + sumToNaive(5));
        System.out.println("倒计时: " + countdown(5));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
