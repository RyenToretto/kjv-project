import java.util.HashSet;
import java.util.Set;

/**
 * [主题]: 至少一次投递与消费者幂等
 * [难度]: Medium～Hard
 * [前置]: 00_async
 *
 * 一、主题说明
 * 多数 MQ 默认偏「至少一次」：可能重复投递。
 * 业务必须幂等：同一消息处理多次，结果与一次相同（去重表 / 唯一键 / 状态机）。
 *
 * 二、写法对比
 * 1. 直观版：每次收到都扣款（重复消息会多扣）。
 * 2. 进阶版：记录已处理 messageId。
 *
 * 三、真实用途
 * - 支付回调、积分发放、库存变更；面试高频。
 */
public class L00_AtLeastOnceIdempotent {

    static class Wallet {
        int balance;

        Wallet(int balance) {
            this.balance = balance;
        }
    }

    /** 危险：无幂等。 */
    public static void creditNaive(Wallet w, int amount) {
        w.balance += amount;
    }

    static class IdempotentConsumer {
        private final Set<String> processed = new HashSet<>();

        boolean credit(Wallet w, String messageId, int amount) {
            if (!processed.add(messageId)) {
                return false; // 重复
            }
            w.balance += amount;
            return true;
        }
    }

    public static void runTests() {
        Wallet a = new Wallet(0);
        creditNaive(a, 100);
        creditNaive(a, 100); // 重复投递
        if (a.balance != 200) {
            throw new AssertionError("无幂等会被加两次");
        }

        Wallet b = new Wallet(0);
        IdempotentConsumer c = new IdempotentConsumer();
        if (!c.credit(b, "msg-1", 100)) {
            throw new AssertionError("首次应处理");
        }
        if (c.credit(b, "msg-1", 100)) {
            throw new AssertionError("重复应跳过");
        }
        if (b.balance != 100) {
            throw new AssertionError("幂等后余额应为 100");
        }
        if (!c.credit(b, "msg-2", 50) || b.balance != 150) {
            throw new AssertionError("新消息应处理");
        }
        System.out.println("✅ AtLeastOnceIdempotent 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 AtLeastOnceIdempotent ************\n");
        Wallet w = new Wallet(0);
        IdempotentConsumer c = new IdempotentConsumer();
        System.out.println("1st=" + c.credit(w, "m", 10) + " bal=" + w.balance);
        System.out.println("2nd=" + c.credit(w, "m", 10) + " bal=" + w.balance);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
