import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: 微服务拆分直觉 —— 单体模块 vs 按边界拆服务
 * [难度]: Medium
 * [前置]: 06_～09_
 *
 * 一、主题说明
 * 不是「把每个类都变成服务」。按业务边界（Bounded Context）拆：用户、订单、库存……
 * 拆分代价：分布式事务、运维、调试更难。小团队可先模块化单体。
 *
 * 二、写法对比
 * 1. 直观版：一个类包办下单+扣库存+积分。
 * 2. 进阶版：Order/Inventory/Points 分服务，经客户端调用（内存模拟）。
 *
 * 三、真实用途
 * - 大厂多服务协作；理解何时不该拆。
 */
public class L00_ServiceSplit {

    static class Monolith {
        int stock = 10;
        int points = 0;

        String placeOrder(String sku, int qty) {
            if (stock < qty) {
                return "FAIL";
            }
            stock -= qty;
            points += qty * 10;
            return "OK";
        }
    }

    static class InventoryService {
        int stock = 10;

        boolean deduct(String sku, int qty) {
            if (stock < qty) {
                return false;
            }
            stock -= qty;
            return true;
        }
    }

    static class PointsService {
        final Map<String, Integer> points = new HashMap<>();

        void add(String userId, int delta) {
            points.merge(userId, delta, Integer::sum);
        }

        int get(String userId) {
            return points.getOrDefault(userId, 0);
        }
    }

    static class OrderService {
        final InventoryService inventory;
        final PointsService points;

        OrderService(InventoryService inventory, PointsService points) {
            this.inventory = inventory;
            this.points = points;
        }

        String place(String userId, String sku, int qty) {
            if (!inventory.deduct(sku, qty)) {
                return "FAIL";
            }
            points.add(userId, qty * 10);
            return "OK";
        }
    }

    public static void runTests() {
        Monolith m = new Monolith();
        if (!"OK".equals(m.placeOrder("s", 2)) || m.stock != 8 || m.points != 20) {
            throw new AssertionError("单体下单失败");
        }

        InventoryService inv = new InventoryService();
        PointsService pts = new PointsService();
        OrderService order = new OrderService(inv, pts);
        if (!"OK".equals(order.place("u1", "s", 2))) {
            throw new AssertionError("拆分下单失败");
        }
        if (inv.stock != 8 || pts.get("u1") != 20) {
            throw new AssertionError("拆分后状态不一致");
        }
        if (!"FAIL".equals(order.place("u1", "s", 100))) {
            throw new AssertionError("库存不足应 FAIL");
        }
        System.out.println("✅ ServiceSplit 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ServiceSplit ************\n");
        OrderService order = new OrderService(new InventoryService(), new PointsService());
        System.out.println(order.place("u", "sku", 1));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
