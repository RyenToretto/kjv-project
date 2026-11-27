import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * [主题]: 消息队列心智 —— 异步解耦与削峰
 * [难度]: Medium
 * [前置]: 06_spring_boot
 *
 * 一、主题说明
 * 同步调用：下单接口里直接发短信/积分，慢且互相拖垮。
 * MQ：生产者把事件丢进队列，消费者异步处理；峰值时队列缓冲（削峰）。
 * RabbitMQ / Kafka 细节不同，本课用内存队列抓住共性。
 *
 * 二、写法对比
 * 1. 直观版：下单方法内同步执行副作用。
 * 2. 进阶版：发消息 + 消费者轮询处理。
 *
 * 三、真实用途
 * - 订单后链路、通知、数据同步、流量削峰。
 */
public class L00_AsyncDecouple {

    record OrderCreated(String orderId, String mobile) {}

    static class SyncOrderService {
        final List<String> smsLog = new ArrayList<>();

        String place(String orderId, String mobile) {
            // 同步发短信（慢）
            smsLog.add("SMS:" + mobile + ":" + orderId);
            return orderId;
        }
    }

    static class AsyncOrderService {
        final Queue<OrderCreated> queue = new ArrayDeque<>();
        final List<String> smsLog = new ArrayList<>();

        String place(String orderId, String mobile) {
            queue.offer(new OrderCreated(orderId, mobile));
            return orderId;
        }

        int drainOnce() {
            int n = 0;
            OrderCreated msg;
            while ((msg = queue.poll()) != null) {
                smsLog.add("SMS:" + msg.mobile() + ":" + msg.orderId());
                n++;
            }
            return n;
        }
    }

    public static void runTests() {
        SyncOrderService sync = new SyncOrderService();
        sync.place("o1", "138");
        if (sync.smsLog.size() != 1) {
            throw new AssertionError("同步应立刻发短信");
        }

        AsyncOrderService async = new AsyncOrderService();
        async.place("o2", "139");
        if (!async.smsLog.isEmpty()) {
            throw new AssertionError("异步下单时短信日志应仍为空");
        }
        if (async.queue.size() != 1) {
            throw new AssertionError("消息应在队列中");
        }
        if (async.drainOnce() != 1 || async.smsLog.size() != 1) {
            throw new AssertionError("消费后应有短信");
        }
        // 削峰：连续放入多条再消费
        async.place("a", "1");
        async.place("b", "1");
        async.place("c", "1");
        if (async.queue.size() != 3) {
            throw new AssertionError("应缓冲 3 条");
        }
        System.out.println("✅ AsyncDecouple 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 AsyncDecouple ************\n");
        AsyncOrderService svc = new AsyncOrderService();
        svc.place("demo", "10086");
        System.out.println("queued=" + svc.queue.size());
        System.out.println("drained=" + svc.drainOnce() + " log=" + svc.smsLog);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
