/**
 * [主题]: 方法定义、参数、返回值与重载（overload）
 * [难度]: Easy
 * [前置]: 00_syntax、01_control_flow
 *
 * 一、主题说明
 * 方法（method）把一段可复用逻辑起名封装。Java 里几乎所有逻辑都写在方法里。
 * 重载：同名方法、参数列表不同（个数或类型），编译器按实参选择哪个版本。
 * 注意：仅返回类型不同不算重载。
 *
 * 二、写法对比
 * 1. 直观版：多个不同名字的方法（addInts / addDoubles）。
 * 2. 进阶版：同名 add 重载；可变参数（varargs）一次接收多个 int。
 *
 * 三、真实用途
 * - Service 层：createOrder(dto)、createOrder(userId, skuId) 等重载 API。
 * - 工具类：String.valueOf 有多种重载；日志框架也大量重载。
 */
public class L00_MethodsAndOverload {

    /** 直观版：只处理 int。 */
    public static int addInts(int a, int b) {
        return a + b;
    }

    /** 直观版：只处理 double。 */
    public static double addDoubles(double a, double b) {
        return a + b;
    }

    /** 进阶版重载 #1 */
    public static int add(int a, int b) {
        return a + b;
    }

    /** 进阶版重载 #2：三个参数 */
    public static int add(int a, int b, int c) {
        return a + b + c;
    }

    /** 进阶版重载 #3：double */
    public static double add(double a, double b) {
        return a + b;
    }

    /**
     * 可变参数：最后一个参数可写成 int... nums，调用时可传 0～多个 int。
     * 方法体内当作 int[] 使用。
     */
    public static int sumAll(int... nums) {
        int sum = 0;
        for (int n : nums) {
            sum += n;
        }
        return sum;
    }

    /** 演示「无返回值」方法：副作用是写入 StringBuilder。 */
    public static void appendTag(StringBuilder sb, String tag) {
        if (tag == null || tag.isBlank()) {
            return; // 提前返回，守卫条款
        }
        sb.append("[").append(tag).append("]");
    }

    public static void runTests() {
        if (addInts(1, 2) != 3) {
            throw new AssertionError("addInts 失败");
        }
        if (Math.abs(addDoubles(1.5, 2.5) - 4.0) > 1e-9) {
            throw new AssertionError("addDoubles 失败");
        }
        if (add(1, 2) != 3) {
            throw new AssertionError("add(int,int) 失败");
        }
        if (add(1, 2, 3) != 6) {
            throw new AssertionError("add(int,int,int) 失败");
        }
        if (Math.abs(add(1.1, 2.2) - 3.3) > 1e-9) {
            throw new AssertionError("add(double,double) 失败");
        }
        if (sumAll() != 0) {
            throw new AssertionError("sumAll() 空参应为 0");
        }
        if (sumAll(1, 2, 3, 4) != 10) {
            throw new AssertionError("sumAll 可变参数失败");
        }
        StringBuilder sb = new StringBuilder("order");
        appendTag(sb, "paid");
        appendTag(sb, "  "); // blank 应忽略
        if (!"order[paid]".equals(sb.toString())) {
            throw new AssertionError("appendTag 结果不符: " + sb);
        }
        System.out.println("✅ MethodsAndOverload 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 MethodsAndOverload ************\n");
        System.out.println("add(2,3)=" + add(2, 3));
        System.out.println("add(2,3,4)=" + add(2, 3, 4));
        System.out.println("sumAll(5,5,5)=" + sumAll(5, 5, 5));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
