import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * [主题]: 切片测试心智 —— MockMvc 风格请求进 Controller、Mock 下游
 * [难度]: Medium
 * [前置]: 11_testing、00_layering
 *
 * 一、主题说明
 * Spring 测试分层：
 * - 纯单元：Mock Service 测 Controller
 * - @WebMvcTest：只起 Web 层（切片）
 * - @SpringBootTest：全容器（慢）
 * 本课实现迷你 MockMvc：发「HTTP」到 Controller，断言状态码与 body。
 *
 * 二、写法对比
 * 1. 直观版：直接 new Service 测（集成感强）。
 * 2. 进阶版：Controller + Fake Service，只验证 Web 适配。
 *
 * 三、真实用途
 * - CI 快速反馈；契约回归。
 */
public class L00_SliceTestStyle {

    record Req(String method, String path, String body) {}

    record Res(int status, String body) {}

    interface OrderFacade {
        String create(String sku, int qty);
    }

    static class OrderController {
        private final OrderFacade facade;

        OrderController(OrderFacade facade) {
            this.facade = facade;
        }

        Res handle(Req req) {
            if (!"POST".equals(req.method()) || !"/orders".equals(req.path())) {
                return new Res(404, "nope");
            }
            // body 格式 sku=xx&qty=n
            String[] parts = req.body().split("&");
            String sku = parts[0].substring("sku=".length());
            int qty = Integer.parseInt(parts[1].substring("qty=".length()));
            if (qty <= 0) {
                return new Res(400, "bad qty");
            }
            try {
                return new Res(201, facade.create(sku, qty));
            } catch (IllegalStateException ex) {
                return new Res(409, ex.getMessage());
            }
        }
    }

    static class MockMvc {
        private final Function<Req, Res> dispatcher;

        MockMvc(Function<Req, Res> dispatcher) {
            this.dispatcher = dispatcher;
        }

        Res perform(Req req) {
            return dispatcher.apply(req);
        }
    }

    static class FakeOrderFacade implements OrderFacade {
        final List<String> calls = new ArrayList<>();

        @Override
        public String create(String sku, int qty) {
            calls.add(sku + ":" + qty);
            if ("bad".equals(sku)) {
                throw new IllegalStateException("stock");
            }
            return "O-" + sku;
        }
    }

    public static void runTests() {
        FakeOrderFacade fake = new FakeOrderFacade();
        MockMvc mvc = new MockMvc(new OrderController(fake)::handle);

        Res ok = mvc.perform(new Req("POST", "/orders", "sku=pen&qty=2"));
        if (ok.status() != 201 || !Objects.equals("O-pen", ok.body())) {
            throw new AssertionError("创建失败: " + ok);
        }
        if (!fake.calls.equals(List.of("pen:2"))) {
            throw new AssertionError("应调用 facade 一次");
        }
        Res badQty = mvc.perform(new Req("POST", "/orders", "sku=pen&qty=0"));
        if (badQty.status() != 400) {
            throw new AssertionError("非法 qty 应 400");
        }
        Res conflict = mvc.perform(new Req("POST", "/orders", "sku=bad&qty=1"));
        if (conflict.status() != 409) {
            throw new AssertionError("库存冲突应 409");
        }
        if (mvc.perform(new Req("GET", "/orders", "")).status() != 404) {
            throw new AssertionError("方法不匹配应 404");
        }
        System.out.println("✅ SliceTestStyle 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 SliceTestStyle ************\n");
        FakeOrderFacade fake = new FakeOrderFacade();
        MockMvc mvc = new MockMvc(new OrderController(fake)::handle);
        System.out.println(mvc.perform(new Req("POST", "/orders", "sku=book&qty=1")));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
