import java.util.ArrayList;
import java.util.List;

/**
 * [主题]: Mock 直觉 —— 用不真实依赖测业务逻辑
 * [难度]: Medium
 * [前置]: 00_junit、06 DI / 07 Repository
 *
 * 一、主题说明
 * 单元测试应快、稳、不连真 DB/HTTP。Mock：按脚本返回预定结果，并记录调用。
 * Mockito 自动化这一点；本课手写 Fake/Mock，理解后再用框架。
 *
 * 二、写法对比
 * 1. 直观版：用真 InMemory 仓库（也是一种测试替身）。
 * 2. 进阶版：可配置返回值的 Mock，断言「是否调用过」。
 *
 * 三、真实用途
 * - Service 单测；Spring @MockBean 切片测试。
 */
public class L00_ManualMock {

    interface SmsClient {
        void send(String mobile, String text);
    }

    static class OrderService {
        private final SmsClient sms;

        OrderService(SmsClient sms) {
            this.sms = sms;
        }

        String place(String mobile, String orderId) {
            sms.send(mobile, "order=" + orderId);
            return orderId;
        }
    }

    /** 记录调用的 Fake。 */
    static class RecordingSms implements SmsClient {
        final List<String> calls = new ArrayList<>();

        @Override
        public void send(String mobile, String text) {
            calls.add(mobile + "|" + text);
        }
    }

    public static void runTests() {
        RecordingSms sms = new RecordingSms();
        OrderService svc = new OrderService(sms);
        if (!"o1".equals(svc.place("138", "o1"))) {
            throw new AssertionError("下单返回值");
        }
        if (sms.calls.size() != 1 || !sms.calls.get(0).equals("138|order=o1")) {
            throw new AssertionError("应记录短信调用: " + sms.calls);
        }
        // 不发短信的「空 Mock」
        OrderService silent = new OrderService((m, t) -> {});
        if (!"o2".equals(silent.place("139", "o2"))) {
            throw new AssertionError("空 mock 仍应返回订单号");
        }
        System.out.println("✅ ManualMock 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ManualMock ************\n");
        RecordingSms sms = new RecordingSms();
        new OrderService(sms).place("10086", "demo");
        System.out.println(sms.calls);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
