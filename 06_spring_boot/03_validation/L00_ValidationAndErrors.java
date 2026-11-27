import java.util.ArrayList;
import java.util.List;

/**
 * [主题]: 参数校验与统一错误响应
 * [难度]: Medium
 * [前置]: 02_rest
 *
 * 一、主题说明
 * 控制器入口必须校验：空值、长度、范围。Spring 常用 @Valid + jakarta.validation。
 * 业务异常不要裸抛给用户：统一包装成 {code,message}，对应 @ControllerAdvice。
 *
 * 二、写法对比
 * 1. 直观版：每个接口手写 if。
 * 2. 进阶版：校验器 + 全局异常翻译。
 *
 * 三、真实用途
 * - 防止脏数据入库；API 错误码对前端友好。
 */
public class L00_ValidationAndErrors {

    record CreateUserReq(String name, int age) {}

    record ApiError(String code, String message) {}

    record ApiResult(int status, String body) {}

    /** 直观版校验。 */
    public static List<String> validateNaive(CreateUserReq req) {
        List<String> errors = new ArrayList<>();
        if (req.name == null || req.name.isBlank()) {
            errors.add("name 必填");
        } else if (req.name.length() > 32) {
            errors.add("name 过长");
        }
        if (req.age < 0 || req.age > 150) {
            errors.add("age 非法");
        }
        return errors;
    }

    static class ValidationException extends RuntimeException {
        final List<String> errors;

        ValidationException(List<String> errors) {
            super(String.join("; ", errors));
            this.errors = errors;
        }
    }

    static class BizException extends RuntimeException {
        final String code;

        BizException(String code, String message) {
            super(message);
            this.code = code;
        }
    }

    public static void validateBetter(CreateUserReq req) {
        List<String> errors = validateNaive(req);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static String createUser(CreateUserReq req, boolean userExists) {
        validateBetter(req);
        if (userExists) {
            throw new BizException("USER_EXISTS", "用户已存在");
        }
        return "{\"name\":\"" + req.name + "\"}";
    }

    /** 模拟 @ControllerAdvice。 */
    public static ApiResult handle(SupplierCall call) {
        try {
            return new ApiResult(200, call.run());
        } catch (ValidationException ex) {
            return new ApiResult(400, "{\"code\":\"VALIDATION\",\"message\":\"" + ex.getMessage() + "\"}");
        } catch (BizException ex) {
            return new ApiResult(409, "{\"code\":\"" + ex.code + "\",\"message\":\"" + ex.getMessage() + "\"}");
        } catch (RuntimeException ex) {
            return new ApiResult(500, "{\"code\":\"INTERNAL\",\"message\":\"系统繁忙\"}");
        }
    }

    @FunctionalInterface
    interface SupplierCall {
        String run();
    }

    public static void runTests() {
        if (!validateNaive(new CreateUserReq("", 10)).contains("name 必填")) {
            throw new AssertionError("空名应报错");
        }
        ApiResult bad = handle(() -> createUser(new CreateUserReq("", 1), false));
        if (bad.status != 400 || !bad.body.contains("VALIDATION")) {
            throw new AssertionError("校验应 400: " + bad);
        }
        ApiResult conflict = handle(() -> createUser(new CreateUserReq("bob", 20), true));
        if (conflict.status != 409) {
            throw new AssertionError("冲突应 409");
        }
        ApiResult ok = handle(() -> createUser(new CreateUserReq("bob", 20), false));
        if (ok.status != 200 || !ok.body.contains("bob")) {
            throw new AssertionError("成功应 200");
        }
        System.out.println("✅ ValidationAndErrors 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ValidationAndErrors ************\n");
        System.out.println(handle(() -> createUser(new CreateUserReq("alice", 18), false)));
        System.out.println(handle(() -> createUser(new CreateUserReq("", 18), false)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
