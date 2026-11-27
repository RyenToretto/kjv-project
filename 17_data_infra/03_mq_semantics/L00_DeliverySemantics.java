import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: 消息投递语义 —— at-most-once / at-least-once / 所谓 exactly-once
 * [难度]: Hard
 * [前置]: 09_message_queue
 *
 * 一、主题说明
 * - at-most-once：最多一次，可能丢，不重复。
 * - at-least-once：至少一次，可能重复，需幂等。
 * - exactly-once：端到端极难；常靠「幂等消费 + 事务/去重」逼近。
 *
 * 二、写法对比
 * 1. 直观版：发出即删（可能未处理就丢）。
 * 2. 进阶版：处理成功才 ack；失败重投；消费者去重。
 *
 * 三、真实用途
 * - Kafka / Rabbit 选型与消费组确认模式；支付类必须幂等。
 */
public class L00_DeliverySemantics {

    record Msg(String id, int amount) {}

    static class Broker {
        final Queue<Msg> q = new ArrayDeque<>();

        void publish(Msg m) {
            q.offer(m);
        }
    }

    /** at-most-once：取出即丢，处理失败也不再有。 */
    public static int consumeAtMostOnce(Broker b, AtomicInteger balance, boolean fail) {
        Msg m = b.q.poll();
        if (m == null) {
            return 0;
        }
        if (fail) {
            return 0; // 丢了
        }
        balance.addAndGet(m.amount());
        return 1;
    }

    /** at-least-once：失败则重新入队。 */
    public static int consumeAtLeastOnce(Broker b, AtomicInteger balance, Set<String> done, boolean failOnce) {
        Msg m = b.q.poll();
        if (m == null) {
            return 0;
        }
        if (failOnce) {
            b.q.offer(m); // 重投
            return 0;
        }
        if (done.add(m.id())) {
            balance.addAndGet(m.amount());
        }
        return 1;
    }

    public static void runTests() {
        Broker lost = new Broker();
        lost.publish(new Msg("m1", 10));
        AtomicInteger bal = new AtomicInteger();
        if (consumeAtMostOnce(lost, bal, true) != 0 || bal.get() != 0 || !lost.q.isEmpty()) {
            throw new AssertionError("at-most-once 失败后消息应已丢");
        }

        Broker retry = new Broker();
        retry.publish(new Msg("m2", 10));
        AtomicInteger bal2 = new AtomicInteger();
        Set<String> done = new HashSet<>();
        if (consumeAtLeastOnce(retry, bal2, done, true) != 0) {
            throw new AssertionError("首次失败不应确认");
        }
        if (retry.q.size() != 1) {
            throw new AssertionError("应重入队");
        }
        if (consumeAtLeastOnce(retry, bal2, done, false) != 1 || bal2.get() != 10) {
            throw new AssertionError("重试成功应入账 10");
        }
        // 再投递一次同 id（重复）
        retry.publish(new Msg("m2", 10));
        consumeAtLeastOnce(retry, bal2, done, false);
        if (bal2.get() != 10) {
            throw new AssertionError("幂等后仍为 10");
        }
        System.out.println("✅ DeliverySemantics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DeliverySemantics ************\n");
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
