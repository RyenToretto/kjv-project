import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * [主题]: API 网关心智 —— 路由、鉴权、聚合入口
 * [难度]: Medium
 * [前置]: 00_split、06 REST
 *
 * 一、主题说明
 * 网关：对外统一入口，按路径转发到不同服务；可集中做鉴权、限流、日志。
 * Spring Cloud Gateway / Nginx 都是这一层。本课用路由表模拟。
 *
 * 二、写法对比
 * 1. 直观版：前端直接调多个服务地址。
 * 2. 进阶版：只调网关；网关校验 token 后转发。
 *
 * 三、真实用途
 * - 微服务对外暴露；BFF；灰度与限流策略落点。
 */
public class L00_ApiGatewayIdea {

    record Req(String path, String token) {}

    record Resp(int status, String body) {}

    static class Gateway {
        private final Map<String, Function<Req, Resp>> routes = new HashMap<>();
        private final String validToken;

        Gateway(String validToken) {
            this.validToken = validToken;
        }

        void route(String prefix, Function<Req, Resp> handler) {
            routes.put(prefix, handler);
        }

        Resp handle(Req req) {
            if (req.token == null || !req.token.equals(validToken)) {
                return new Resp(401, "unauthorized");
            }
            for (var e : routes.entrySet()) {
                if (req.path.startsWith(e.getKey())) {
                    return e.getValue().apply(req);
                }
            }
            return new Resp(404, "no route");
        }
    }

    public static Gateway sample() {
        Gateway gw = new Gateway("secret");
        gw.route("/users", r -> new Resp(200, "user-svc:" + r.path));
        gw.route("/orders", r -> new Resp(200, "order-svc:" + r.path));
        return gw;
    }

    public static void runTests() {
        Gateway gw = sample();
        if (gw.handle(new Req("/users/1", null)).status != 401) {
            throw new AssertionError("无 token 应 401");
        }
        Resp u = gw.handle(new Req("/users/1", "secret"));
        if (u.status != 200 || !u.body.contains("user-svc")) {
            throw new AssertionError("用户路由失败: " + u);
        }
        Resp o = gw.handle(new Req("/orders/9", "secret"));
        if (!o.body.contains("order-svc")) {
            throw new AssertionError("订单路由失败");
        }
        if (gw.handle(new Req("/other", "secret")).status != 404) {
            throw new AssertionError("未知前缀应 404");
        }
        System.out.println("✅ ApiGatewayIdea 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ApiGatewayIdea ************\n");
        System.out.println(sample().handle(new Req("/users/42", "secret")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
