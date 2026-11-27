import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: CQRS 入门 —— 命令模型与查询模型分离
 * [难度]: Hard
 * [前置]: 00_ddd、01_events
 *
 * 一、主题说明
 * CQRS：写（Command）与读（Query）用不同模型/存储。
 * 写侧保证不变式；读侧可做扁平投影加速查询。
 * 本课：下单写聚合；投影表支撑「我的订单列表」。
 *
 * 二、写法对比
 * 1. 直观版：同一张表既写又复杂 join 读。
 * 2. 进阶版：命令更新写库，事件刷新读库。
 *
 * 三、真实用途
 * - 高读低写报表；复杂域拆分；注意最终一致。
 */
public class L00_CqrsSplit {

    record PlaceOrder(String orderId, String userId, String sku, int qty) {}

    static class WriteModel {
        final Map<String, String> orders = new HashMap<>(); // id -> userId|sku|qty

        void handle(PlaceOrder cmd) {
            if (orders.containsKey(cmd.orderId())) {
                throw new IllegalStateException("重复下单");
            }
            if (cmd.qty() <= 0) {
                throw new IllegalArgumentException("qty");
            }
            orders.put(cmd.orderId(), cmd.userId() + "|" + cmd.sku() + "|" + cmd.qty());
        }
    }

    record OrderView(String orderId, String sku, int qty) {}

    static class ReadModel {
        final Map<String, List<OrderView>> byUser = new HashMap<>();

        void onPlaced(PlaceOrder cmd) {
            byUser.computeIfAbsent(cmd.userId(), k -> new ArrayList<>())
                    .add(new OrderView(cmd.orderId(), cmd.sku(), cmd.qty()));
        }

        List<OrderView> listByUser(String userId) {
            return List.copyOf(byUser.getOrDefault(userId, List.of()));
        }
    }

    static class App {
        final WriteModel write = new WriteModel();
        final ReadModel read = new ReadModel();

        void place(PlaceOrder cmd) {
            write.handle(cmd);
            read.onPlaced(cmd); // 同步投影；生产常异步/Outbox
        }
    }

    public static void runTests() {
        App app = new App();
        app.place(new PlaceOrder("o1", "u1", "book", 1));
        app.place(new PlaceOrder("o2", "u1", "pen", 2));
        app.place(new PlaceOrder("o3", "u2", "book", 1));
        List<OrderView> u1 = app.read.listByUser("u1");
        if (u1.size() != 2 || !"book".equals(u1.get(0).sku())) {
            throw new AssertionError("读模型 u1 不对: " + u1);
        }
        if (app.write.orders.size() != 3) {
            throw new AssertionError("写模型应有 3 单");
        }
        boolean dup = false;
        try {
            app.place(new PlaceOrder("o1", "u1", "x", 1));
        } catch (IllegalStateException ex) {
            dup = true;
        }
        if (!dup) {
            throw new AssertionError("重复命令应失败");
        }
        System.out.println("✅ CqrsSplit 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CqrsSplit ************\n");
        App app = new App();
        app.place(new PlaceOrder("d1", "alice", "sku", 1));
        System.out.println(app.read.listByUser("alice"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
