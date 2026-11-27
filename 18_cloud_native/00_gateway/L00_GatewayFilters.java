import java.util.List;

/**
 * [主题]: 网关过滤器链 —— 鉴权 / 限流 / 改写路径
 * [难度]: Medium～Hard
 * [前置]: 10_gateway、12_chain
 *
 * 一、主题说明
 * Spring Cloud Gateway：GlobalFilter + 路由谓词。
 * 本课把请求对象沿过滤器链传递，可短路返回。
 *
 * 二、写法对比
 * 1. 直观版：Controller 前手写 if。
 * 2. 进阶版：可插拔 Filter 链（鉴权→限流→改写）。
 *
 * 三、真实用途
 * - 统一鉴权、灰度、跨域、请求日志。
 */
public class L00_GatewayFilters {

    record GwRequest(String path, String token, int qps) {
        GwRequest withPath(String p) {
            return new GwRequest(p, token, qps);
        }
    }

    record GwResponse(int status, String body) {}

    @FunctionalInterface
    interface Chain {
        GwResponse next(GwRequest req);
    }

    @FunctionalInterface
    interface Filter {
        GwResponse apply(GwRequest req, Chain chain);
    }

    /** 直观版：巨型 if。 */
    public static GwResponse dispatchNaive(GwRequest req) {
        if (req.token() == null) {
            return new GwResponse(401, "auth");
        }
        if (req.qps() > 100) {
            return new GwResponse(429, "limit");
        }
        return new GwResponse(200, "OK:/svc" + req.path());
    }

    public static GwResponse dispatch(List<Filter> filters, GwRequest req) {
        return new Object() {
            GwResponse invoke(int i, GwRequest r) {
                if (i == filters.size()) {
                    return new GwResponse(200, "OK:" + r.path());
                }
                return filters.get(i).apply(r, nr -> invoke(i + 1, nr));
            }
        }.invoke(0, req);
    }

    public static List<Filter> defaultFilters() {
        return List.of(
                (req, chain) -> req.token() == null ? new GwResponse(401, "auth") : chain.next(req),
                (req, chain) -> req.qps() > 100 ? new GwResponse(429, "limit") : chain.next(req),
                (req, chain) -> chain.next(req.withPath("/svc" + req.path()))
        );
    }

    public static void runTests() {
        List<Filter> filters = defaultFilters();
        if (dispatch(filters, new GwRequest("/a", null, 1)).status() != 401) {
            throw new AssertionError("应 401");
        }
        if (dispatch(filters, new GwRequest("/a", "t", 200)).status() != 429) {
            throw new AssertionError("应 429");
        }
        GwResponse ok = dispatch(filters, new GwRequest("/a", "t", 1));
        if (ok.status() != 200 || !ok.body().equals("OK:/svc/a")) {
            throw new AssertionError("路径改写失败: " + ok);
        }
        if (dispatchNaive(new GwRequest("/a", "t", 1)).status() != 200) {
            throw new AssertionError("naive 成功路径");
        }
        System.out.println("✅ GatewayFilters 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 GatewayFilters ************\n");
        System.out.println(dispatch(defaultFilters(), new GwRequest("/orders", "tok", 3)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
