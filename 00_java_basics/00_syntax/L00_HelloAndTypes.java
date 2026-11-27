/**
 * [主题]: 第一个 Java 程序：main 入口、基本类型与算术、自测结构
 * [难度]: Easy
 * [前置]: 无（Java 小白第一课）
 *
 * 一、主题说明
 * Java 程序从 public static void main(String[] args) 开始执行。
 * 本课建立「写逻辑 → bootstrap 演示 → runTests 断言」的学习习惯，与仓库其它课一致。
 *
 * 二、写法对比
 * 1. 直观版 addNaive: 直接用 + 号，适合入门。
 * 2. 进阶版 addBetter: Math.addExact，溢出时抛异常，避免静默算错。
 *
 * 三、真实用途
 * - 所有后端服务、定时任务、命令行工具都有入口方法。
 * - 数值计算在订单金额、库存扣减里必须警惕溢出与精度（金额常用 BigDecimal，后续课会讲）。
 */
public class L00_HelloAndTypes {

    /** 直观版：普通整数相加。 */
    public static int addNaive(int left, int right) {
        return left + right;
    }

    /** 进阶版：溢出则失败，而不是绕回负数。 */
    public static int addBetter(int left, int right) {
        return Math.addExact(left, right);
    }

    /** 演示基本类型声明（只读，供 bootstrap 打印）。 */
    public static String describePrimitives() {
        int age = 18;
        double price = 19.9;
        boolean ok = true;
        char grade = 'A';
        return "age=" + age + ", price=" + price + ", ok=" + ok + ", grade=" + grade;
    }

    public static void runTests() {
        if (addNaive(1, 2) != 3) {
            throw new AssertionError("addNaive(1,2) 应为 3");
        }
        if (addBetter(2, 3) != 5) {
            throw new AssertionError("addBetter(2,3) 应为 5");
        }
        // 溢出：Integer.MAX_VALUE + 1 用 addExact 必须失败
        boolean overflowCaught = false;
        try {
            addBetter(Integer.MAX_VALUE, 1);
        } catch (ArithmeticException ex) {
            overflowCaught = true;
        }
        if (!overflowCaught) {
            throw new AssertionError("addBetter 溢出时应抛 ArithmeticException");
        }
        if (!describePrimitives().contains("age=18")) {
            throw new AssertionError("describePrimitives 应包含 age=18");
        }
        System.out.println("✅ HelloAndTypes 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 HelloAndTypes ************\n");
        System.out.println("基本类型示例: " + describePrimitives());
        System.out.println("1 + 2 = " + addNaive(1, 2));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
