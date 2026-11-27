/**
 * [主题]: 测试断言风格 —— 从手写 AssertionError 到 JUnit 心智
 * [难度]: Easy～Medium
 * [前置]: 00_java_basics（本仓库课都自带 runTests）
 *
 * 一、主题说明
 * 本仓库用 if + AssertionError，不依赖测试框架即可跑。
 * JUnit 5：@Test 方法 + Assertions.assertEquals；Surefire 批量跑。
 * 本课对照两种风格，并演示「测试命名：给定_当_则」。
 *
 * 二、写法对比
 * 1. 直观版：仓库风格 runTests。
 * 2. 进阶版：仿 JUnit 的 assertThat 小工具（仍无第三方，verify 可跑）。
 *
 * 三、真实用途
 * - 团队 CI；回归保护；TDD。
 */
public class L00_AssertionStyle {

    public static int add(int a, int b) {
        return a + b;
    }

    public static int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("div0");
        }
        return a / b;
    }

    /** 仿 assertEquals。 */
    public static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(msg + " expected=" + expected + " actual=" + actual);
        }
    }

    public static void assertThrows(Class<? extends RuntimeException> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + " 未抛异常");
        } catch (RuntimeException ex) {
            if (!type.isInstance(ex)) {
                throw new AssertionError(msg + " 类型不符: " + ex.getClass());
            }
        }
    }

    /** 给定两正数_当相加_则得到和 */
    public static void test_add_twoPositives() {
        assertEquals(5, add(2, 3), "2+3");
    }

    public static void test_divide_byZero() {
        assertThrows(IllegalArgumentException.class, () -> divide(1, 0), "除零");
    }

    public static void runTests() {
        test_add_twoPositives();
        test_divide_byZero();
        if (add(0, 0) != 0) {
            throw new AssertionError("0+0");
        }
        System.out.println("✅ AssertionStyle 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 AssertionStyle ************\n");
        System.out.println("add(2,3)=" + add(2, 3));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
