import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [主题]: 统一错误体 —— Problem Details / 业务错误码
 * [难度]: Medium
 * [前置]: 06_validation、00_layering
 *
 * 一、主题说明
 * 生产 API 不要把堆栈直接回给前端。常用 RFC 7807 Problem Details，或公司内 {code,message,traceId}。
 * Spring 6+：ProblemDetail；@ControllerAdvice 统一转换。
 *
 * 二、写法对比
 * 1. 直观版：各 Controller catch 后返回字符串。
 * 2. 进阶版：全局 Handler 映射异常 → 标准 JSON。
 *
 * 三、真实用途
 * - 前后端契约稳定；便于监控按 code 聚合。
 */
public class L00_ProblemDetails {

    static class BizException extends RuntimeException {
        final String code;
        final int status;

        BizException(String code, int status, String message) {
            super(message);
            this.code = code;
            this.status = status;
        }
    }

    static class ValidationException extends RuntimeException {
        ValidationException(String message) {
            super(message);
        }
    }

    record Problem(int status, String code, String message, String path, String traceId) {
        Map<String, Object> toJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", status);
            m.put("code", code);
            m.put("message", message);
            m.put("path", path);
            m.put("traceId", traceId);
            return m;
        }
    }

    static class Advice {
        Problem handle(String path, String traceId, RuntimeException ex) {
            if (ex instanceof BizException biz) {
                return new Problem(biz.status, biz.code, biz.getMessage(), path, traceId);
            }
            if (ex instanceof ValidationException) {
                return new Problem(400, "VALIDATION", ex.getMessage(), path, traceId);
            }
            return new Problem(500, "INTERNAL", "系统繁忙", path, traceId);
        }
    }

    public static String invoke(String path, Runnable action) {
        Advice advice = new Advice();
        try {
            action.run();
            return "OK";
        } catch (RuntimeException ex) {
            return advice.handle(path, "tid-demo", ex).toJson().toString();
        }
    }

    public static void runTests() {
        String v = invoke("/api/x", () -> {
            throw new ValidationException("name 必填");
        });
        if (!v.contains("VALIDATION") || !v.contains("400")) {
            throw new AssertionError("校验错误体不对: " + v);
        }
        String b = invoke("/api/order", () -> {
            throw new BizException("STOCK_NOT_ENOUGH", 409, "库存不足");
        });
        if (!b.contains("STOCK_NOT_ENOUGH") || !b.contains("409")) {
            throw new AssertionError("业务错误体不对: " + b);
        }
        String i = invoke("/api/y", () -> {
            throw new RuntimeException("boom");
        });
        if (!i.contains("INTERNAL") || i.contains("boom")) {
            throw new AssertionError("内部错误不应泄露细节: " + i);
        }
        if (!"OK".equals(invoke("/api/z", () -> {}))) {
            throw new AssertionError("成功路径");
        }
        System.out.println("✅ ProblemDetails 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ProblemDetails ************\n");
        System.out.println(invoke("/api/demo", () -> {
            throw new BizException("USER_EXISTS", 409, "用户已存在");
        }));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
