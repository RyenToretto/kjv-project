import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * [主题]: REST 控制器模式 —— 路由、HTTP 方法、JSON 响应心智
 * [难度]: Medium
 * [前置]: 01_di
 *
 * 一、主题说明
 * REST API：用 URL + GET/POST/PUT/DELETE 表达资源操作。
 * Spring MVC：@RestController + @GetMapping("/users/{id}") 把请求映射到方法。
 * 本课实现迷你 Dispatcher：根据 method+path 找处理器。
 *
 * 二、写法对比
 * 1. 直观版：巨型 if-else 判断 path。
 * 2. 进阶版：路由表注册；路径变量 {id}。
 *
 * 三、真实用途
 * - 前后端分离后端的标准入口；统一返回结构、状态码。
 */
public class L00_RestControllerPattern {

    record HttpRequest(String method, String path) {}

    record HttpResponse(int status, String body) {}

    /** 直观版。 */
    public static HttpResponse dispatchNaive(HttpRequest req) {
        if ("GET".equals(req.method) && req.path.equals("/health")) {
            return new HttpResponse(200, "{\"ok\":true}");
        }
        if ("GET".equals(req.method) && req.path.startsWith("/users/")) {
            String id = req.path.substring("/users/".length());
            return new HttpResponse(200, "{\"id\":\"" + id + "\"}");
        }
        return new HttpResponse(404, "{\"error\":\"not found\"}");
    }

    static class MiniMvc {
        private final Map<String, Function<Map<String, String>, HttpResponse>> routes = new HashMap<>();

        void get(String pattern, Function<Map<String, String>, HttpResponse> handler) {
            routes.put("GET " + pattern, handler);
        }

        HttpResponse dispatch(HttpRequest req) {
            for (var e : routes.entrySet()) {
                String key = e.getKey();
                if (!key.startsWith(req.method + " ")) {
                    continue;
                }
                String pattern = key.substring((req.method + " ").length());
                Map<String, String> vars = match(pattern, req.path);
                if (vars != null) {
                    return e.getValue().apply(vars);
                }
            }
            return new HttpResponse(404, "{\"error\":\"not found\"}");
        }

        static Map<String, String> match(String pattern, String path) {
            String[] pp = pattern.split("/");
            String[] pa = path.split("/");
            if (pp.length != pa.length) {
                return null;
            }
            Map<String, String> vars = new HashMap<>();
            for (int i = 0; i < pp.length; i++) {
                if (pp[i].startsWith("{") && pp[i].endsWith("}")) {
                    vars.put(pp[i].substring(1, pp[i].length() - 1), pa[i]);
                } else if (!pp[i].equals(pa[i])) {
                    return null;
                }
            }
            return vars;
        }
    }

    public static MiniMvc sampleApp() {
        MiniMvc mvc = new MiniMvc();
        mvc.get("/health", vars -> new HttpResponse(200, "{\"ok\":true}"));
        mvc.get("/users/{id}", vars -> new HttpResponse(200, "{\"id\":\"" + vars.get("id") + "\"}"));
        return mvc;
    }

    public static void runTests() {
        if (dispatchNaive(new HttpRequest("GET", "/health")).status != 200) {
            throw new AssertionError("health 应为 200");
        }
        MiniMvc app = sampleApp();
        HttpResponse u = app.dispatch(new HttpRequest("GET", "/users/42"));
        if (u.status != 200 || !u.body.contains("42")) {
            throw new AssertionError("路径变量失败: " + u);
        }
        if (app.dispatch(new HttpRequest("GET", "/nope")).status != 404) {
            throw new AssertionError("未知路径应为 404");
        }
        if (dispatchNaive(new HttpRequest("GET", "/users/7")).body.contains("7")
                && app.dispatch(new HttpRequest("GET", "/users/7")).body.contains("7")) {
            // ok
        } else {
            throw new AssertionError("两版 /users/7 都应含 7");
        }
        System.out.println("✅ RestControllerPattern 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 RestControllerPattern ************\n");
        System.out.println(sampleApp().dispatch(new HttpRequest("GET", "/users/1")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
