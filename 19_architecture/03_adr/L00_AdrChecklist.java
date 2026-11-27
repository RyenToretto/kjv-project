import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: ADR（架构决策记录）—— 可执行检查清单
 * [难度]: Medium
 * [前置]: 14～18
 *
 * 一、主题说明
 * ADR：记录「为什么选这个方案」：上下文、决策、后果。
 * 本课用结构化对象表示 ADR，并校验必填字段；附带一份样例决策。
 *
 * 二、写法对比
 * 1. 直观版：口头约定，人走茶凉。
 * 2. 进阶版：仓库内 ADR-0001.md 风格的结构化记录。
 *
 * 三、真实用途
 * - 团队对齐；新人 onboarding；避免重复争论。
 */
public class L00_AdrChecklist {

    record Adr(String id, String title, String status, String context, String decision, List<String> consequences) {}

    public static List<String> validate(Adr adr) {
        List<String> errors = new ArrayList<>();
        if (adr.id() == null || !adr.id().startsWith("ADR-")) {
            errors.add("id 需 ADR- 前缀");
        }
        if (blank(adr.title())) {
            errors.add("title 必填");
        }
        if (!List.of("Proposed", "Accepted", "Deprecated", "Superseded").contains(adr.status())) {
            errors.add("status 非法");
        }
        if (blank(adr.context()) || blank(adr.decision())) {
            errors.add("context/decision 必填");
        }
        if (adr.consequences() == null || adr.consequences().isEmpty()) {
            errors.add("consequences 至少一条");
        }
        return errors;
    }

    public static Adr sampleJwtChoice() {
        return new Adr(
                "ADR-0001",
                "API 使用 JWT 而非服务端 Session",
                "Accepted",
                "前后端分离，需水平扩展，多实例不能粘滞 Session。",
                "采用无状态 JWT（短期 access + 刷新策略后续 ADR）。",
                List.of("网关/服务需统一验签", "注销需黑名单或短 TTL", "密钥轮换流程要补齐")
        );
    }

    public static Map<String, String> toMarkdownFront(Adr adr) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", adr.id());
        m.put("title", adr.title());
        m.put("status", adr.status());
        return m;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    public static void runTests() {
        Adr ok = sampleJwtChoice();
        if (!validate(ok).isEmpty()) {
            throw new AssertionError("样例应合法: " + validate(ok));
        }
        Adr bad = new Adr("X", "", "Nope", "", "", List.of());
        List<String> errs = validate(bad);
        if (errs.size() < 4) {
            throw new AssertionError("应检出多项错误: " + errs);
        }
        if (!"ADR-0001".equals(toMarkdownFront(ok).get("id"))) {
            throw new AssertionError("front matter");
        }
        System.out.println("✅ AdrChecklist 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 AdrChecklist ************\n");
        Adr adr = sampleJwtChoice();
        System.out.println(toMarkdownFront(adr));
        System.out.println(adr.decision());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
