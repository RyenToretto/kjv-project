import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [主题]: DDD 战术设计 —— 实体 / 值对象 / 聚合根
 * [难度]: Hard
 * [前置]: 13 迷你商店、14 分层
 *
 * 一、主题说明
 * - 实体（Entity）：有唯一标识，可变
 * - 值对象（Value Object）：无标识，不可变，按值相等
 * - 聚合根（Aggregate Root）：对外一致性边界，外部只通过根修改内部
 * 对照商店域：Order 是聚合根，OrderLine 是实体，Money 是值对象。
 *
 * 二、写法对比
 * 1. 直观版：贫血 Map/DTO 到处改字段。
 * 2. 进阶版：行为留在聚合内（addLine / pay）。
 *
 * 三、真实用途
 * - 复杂业务防「事务脚本」膨胀；与 Spring 分层互补。
 */
public class L00_DddTactical {

    /** 值对象：钱。 */
    record Money(int cents, String currency) {
        Money {
            if (cents < 0) {
                throw new IllegalArgumentException("cents");
            }
            currency = currency == null ? "CNY" : currency;
        }

        Money plus(Money other) {
            if (!currency.equals(other.currency)) {
                throw new IllegalArgumentException("currency");
            }
            return new Money(cents + other.cents, currency);
        }
    }

    /** 实体：订单行。 */
    static final class OrderLine {
        private final String sku;
        private int qty;
        private final Money unitPrice;

        OrderLine(String sku, int qty, Money unitPrice) {
            this.sku = Objects.requireNonNull(sku);
            if (qty <= 0) {
                throw new IllegalArgumentException("qty");
            }
            this.qty = qty;
            this.unitPrice = unitPrice;
        }

        Money lineAmount() {
            return new Money(unitPrice.cents() * qty, unitPrice.currency());
        }

        String sku() {
            return sku;
        }

        int qty() {
            return qty;
        }
    }

    /** 聚合根：订单。 */
    static final class Order {
        private final String id;
        private final List<OrderLine> lines = new ArrayList<>();
        private String status = "NEW";

        Order(String id) {
            this.id = id;
        }

        void addLine(String sku, int qty, Money price) {
            ensureNew();
            lines.add(new OrderLine(sku, qty, price));
        }

        Money total() {
            Money sum = new Money(0, "CNY");
            for (OrderLine line : lines) {
                sum = sum.plus(line.lineAmount());
            }
            return sum;
        }

        void pay() {
            ensureNew();
            if (lines.isEmpty()) {
                throw new IllegalStateException("空订单");
            }
            status = "PAID";
        }

        private void ensureNew() {
            if (!"NEW".equals(status)) {
                throw new IllegalStateException("状态不允许: " + status);
            }
        }

        String id() {
            return id;
        }

        String status() {
            return status;
        }

        int lineCount() {
            return lines.size();
        }
    }

    public static void runTests() {
        Money a = new Money(100, "CNY");
        Money b = new Money(100, "CNY");
        if (!a.equals(b)) {
            throw new AssertionError("值对象按值相等");
        }
        Order order = new Order("O1");
        order.addLine("SKU1", 2, new Money(500, "CNY"));
        order.addLine("SKU2", 1, new Money(200, "CNY"));
        if (order.total().cents() != 1200) {
            throw new AssertionError("合计应为 1200");
        }
        order.pay();
        if (!"PAID".equals(order.status())) {
            throw new AssertionError("支付后 PAID");
        }
        boolean blocked = false;
        try {
            order.addLine("SKU3", 1, new Money(1, "CNY"));
        } catch (IllegalStateException ex) {
            blocked = true;
        }
        if (!blocked) {
            throw new AssertionError("已支付不可再加行");
        }
        System.out.println("✅ DddTactical 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DddTactical ************\n");
        Order order = new Order("demo");
        order.addLine("book", 1, new Money(3000, "CNY"));
        System.out.println(order.id() + " total=" + order.total());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
