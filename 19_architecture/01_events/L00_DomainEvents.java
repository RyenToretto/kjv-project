import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * [主题]: 领域事件 —— 聚合内收集、事务后发布
 * [难度]: Hard
 * [前置]: 00_ddd、09_mq
 *
 * 一、主题说明
 * 领域事件：业务上「已发生的事实」（OrderPaid）。
 * 聚合方法内登记事件，应用服务在事务提交后派发给处理器 / MQ。
 *
 * 二、写法对比
 * 1. 直观版：pay() 里直接调短信/积分（耦合）。
 * 2. 进阶版：只追加事件，外层 dispatcher 处理。
 *
 * 三、真实用途
 * - 解耦副作用；事件溯源入门；Outbox 模式基础。
 */
public class L00_DomainEvents {

    interface DomainEvent {
        String type();
    }

    record OrderPaid(String orderId, int amountCents) implements DomainEvent {
        @Override
        public String type() {
            return "OrderPaid";
        }
    }

    static class Order {
        private final String id;
        private String status = "NEW";
        private final List<DomainEvent> events = new ArrayList<>();

        Order(String id) {
            this.id = id;
        }

        void pay(int amountCents) {
            if (!"NEW".equals(status)) {
                throw new IllegalStateException("不可重复支付");
            }
            status = "PAID";
            events.add(new OrderPaid(id, amountCents));
        }

        List<DomainEvent> pullEvents() {
            List<DomainEvent> copy = List.copyOf(events);
            events.clear();
            return copy;
        }

        String status() {
            return status;
        }
    }

    static class EventBus {
        private final List<Consumer<DomainEvent>> handlers = new ArrayList<>();
        final List<String> log = new ArrayList<>();

        void subscribe(Consumer<DomainEvent> h) {
            handlers.add(h);
        }

        void publish(List<DomainEvent> events) {
            for (DomainEvent e : events) {
                for (Consumer<DomainEvent> h : handlers) {
                    h.accept(e);
                }
            }
        }
    }

    public static void runTests() {
        Order order = new Order("O9");
        order.pay(1000);
        List<DomainEvent> events = order.pullEvents();
        if (events.size() != 1 || !"OrderPaid".equals(events.get(0).type())) {
            throw new AssertionError("应产生 OrderPaid");
        }
        if (!order.pullEvents().isEmpty()) {
            throw new AssertionError("pull 后应清空");
        }

        EventBus bus = new EventBus();
        bus.subscribe(e -> {
            if (e instanceof OrderPaid paid) {
                bus.log.add("SMS:" + paid.orderId());
            }
        });
        bus.subscribe(e -> {
            if (e instanceof OrderPaid paid) {
                bus.log.add("POINT:" + paid.amountCents());
            }
        });
        Order o2 = new Order("O10");
        o2.pay(500);
        bus.publish(o2.pullEvents());
        if (!bus.log.equals(List.of("SMS:O10", "POINT:500"))) {
            throw new AssertionError("总线日志不对: " + bus.log);
        }
        System.out.println("✅ DomainEvents 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DomainEvents ************\n");
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
