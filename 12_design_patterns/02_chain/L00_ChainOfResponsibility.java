/**
 * [主题]: 责任链 —— 过滤器/拦截器流水线
 * [难度]: Medium
 * [前置]: 06 校验与 REST
 *
 * 一、主题说明
 * 责任链：请求沿处理器链表传递，直到被处理或走完。
 * Servlet Filter、Spring Interceptor、Netty Pipeline 都是变体。
 *
 * 二、写法对比
 * 1. 直观版：controller 里顺序调用 checkAuth/checkRate…。
 * 2. 进阶版：可插拔 Handler 链。
 *
 * 三、真实用途
 * - 鉴权→限流→日志→业务；网关插件。
 */
public class L00_ChainOfResponsibility {

    record Request(String token, int qps) {}

    record Response(int status, String body) {}

    interface Handler {
        Response handle(Request req);
    }

    static class AuthHandler implements Handler {
        private final Handler next;

        AuthHandler(Handler next) {
            this.next = next;
        }

        @Override
        public Response handle(Request req) {
            if (req.token == null || req.token.isBlank()) {
                return new Response(401, "no auth");
            }
            return next.handle(req);
        }
    }

    static class RateLimitHandler implements Handler {
        private final Handler next;
        private final int maxQps;

        RateLimitHandler(Handler next, int maxQps) {
            this.next = next;
            this.maxQps = maxQps;
        }

        @Override
        public Response handle(Request req) {
            if (req.qps > maxQps) {
                return new Response(429, "too many");
            }
            return next.handle(req);
        }
    }

    static class BizHandler implements Handler {
        @Override
        public Response handle(Request req) {
            return new Response(200, "ok");
        }
    }

    public static Handler buildChain(int maxQps) {
        return new AuthHandler(new RateLimitHandler(new BizHandler(), maxQps));
    }

    public static void runTests() {
        Handler chain = buildChain(10);
        if (chain.handle(new Request(null, 1)).status != 401) {
            throw new AssertionError("应 401");
        }
        if (chain.handle(new Request("t", 99)).status != 429) {
            throw new AssertionError("应 429");
        }
        if (chain.handle(new Request("t", 1)).status != 200) {
            throw new AssertionError("应 200");
        }
        System.out.println("✅ ChainOfResponsibility 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ChainOfResponsibility ************\n");
        System.out.println(buildChain(5).handle(new Request("tok", 1)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
