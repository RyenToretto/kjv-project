/**
 * [主题]: 异常处理 —— try/catch/finally、checked vs unchecked、自定义异常
 * [难度]: Medium
 * [前置]: 04_oop_basics～06_interfaces
 *
 * 一、主题说明
 * 异常是 Java 报告「可恢复或需上层处理的错误」的标准方式。
 * - RuntimeException 及子类：非受检（unchecked），可不声明 throws。
 * - 其它 Exception：受检（checked），必须 catch 或 throws。
 * 现代业务代码更多用非受检 + 统一错误处理；读懂 checked 仍必要（IO、JDBC 常见）。
 *
 * 二、写法对比
 * 1. 直观版：返回错误码 / null（调用方容易忘检查）。
 * 2. 进阶版：抛异常；try-with-resources 自动关闭资源（后续 JDBC 会再用）。
 *
 * 三、真实用途
 * - 参数非法 → IllegalArgumentException
 * - 业务规则 → 自定义业务异常（如余额不足）
 * - Spring 可用 @ControllerAdvice 统一转 HTTP 错误响应
 */

/** 业务：余额不足。 */
class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

/** 演示受检异常：模拟「必须处理」的检查。 */
class InvalidFormatException extends Exception {
    public InvalidFormatException(String message) {
        super(message);
    }
}

public class L00_ExceptionHandling {

    /** 直观版：用特殊返回值 -1 表示失败（歧义：真的扣到 -1 怎么办？）。 */
    public static int withdrawNaive(int balance, int amount) {
        if (amount <= 0 || amount > balance) {
            return -1;
        }
        return balance - amount;
    }

    /** 进阶版：失败抛异常，成功返回新余额。 */
    public static int withdrawBetter(int balance, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount 必须 > 0");
        }
        if (amount > balance) {
            throw new InsufficientBalanceException(
                    "余额 " + balance + " 不足以支付 " + amount);
        }
        return balance - amount;
    }

    /** 受检异常：解析正整数，格式不对必须声明或捕获。 */
    public static int parsePositive(String raw) throws InvalidFormatException {
        if (raw == null || raw.isBlank()) {
            throw new InvalidFormatException("空字符串");
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                throw new InvalidFormatException("必须为正整数: " + raw);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new InvalidFormatException("不是整数: " + raw);
        }
    }

    /** 包装：把受检异常转成友好结果，避免调用方强制 throws。 */
    public static int parsePositiveOrZero(String raw) {
        try {
            return parsePositive(raw);
        } catch (InvalidFormatException ex) {
            return 0;
        }
    }

    /** finally：无论是否异常都会执行（常用于释放锁/计数；关资源优先 try-with-resources）。 */
    public static String withFinallyFlag() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("try");
            throw new RuntimeException("boom");
        } catch (RuntimeException ex) {
            sb.append("-catch");
        } finally {
            sb.append("-finally");
        }
        return sb.toString();
    }

    public static void runTests() {
        if (withdrawNaive(100, 30) != 70) {
            throw new AssertionError("直观版取款失败");
        }
        if (withdrawNaive(100, 200) != -1) {
            throw new AssertionError("直观版超额应返回 -1");
        }

        if (withdrawBetter(100, 30) != 70) {
            throw new AssertionError("进阶版取款失败");
        }
        boolean insufficient = false;
        try {
            withdrawBetter(100, 200);
        } catch (InsufficientBalanceException ex) {
            insufficient = true;
        }
        if (!insufficient) {
            throw new AssertionError("超额应抛 InsufficientBalanceException");
        }

        boolean badAmount = false;
        try {
            withdrawBetter(100, 0);
        } catch (IllegalArgumentException ex) {
            badAmount = true;
        }
        if (!badAmount) {
            throw new AssertionError("amount<=0 应抛 IllegalArgumentException");
        }

        try {
            if (parsePositive("42") != 42) {
                throw new AssertionError("parsePositive 42 失败");
            }
        } catch (InvalidFormatException ex) {
            throw new AssertionError("42 不应失败", ex);
        }
        if (parsePositiveOrZero("xx") != 0) {
            throw new AssertionError("非法输入应得到 0");
        }
        if (!"try-catch-finally".equals(withFinallyFlag())) {
            throw new AssertionError("finally 未按预期执行: " + withFinallyFlag());
        }
        System.out.println("✅ ExceptionHandling 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ExceptionHandling ************\n");
        System.out.println("withdrawBetter(100,40)=" + withdrawBetter(100, 40));
        System.out.println("parsePositiveOrZero('9')=" + parsePositiveOrZero("9"));
        System.out.println("finally 演示: " + withFinallyFlag());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
