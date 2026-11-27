/**
 * [主题]: 接口（interface）与抽象类（abstract class）
 * [难度]: Medium
 * [前置]: 05_inheritance
 *
 * 一、主题说明
 * 抽象类：可以有字段、构造器、已实现方法 + abstract 方法；单继承。
 * 接口：描述「能做什么」的契约；一个类可实现多个接口；Java 8+ 可有 default 方法。
 * 选型直觉：共享状态/模板算法 → 抽象类；能力插件/多角色 → 接口。
 *
 * 二、写法对比
 * 1. 直观版：只有具体类，调用方写死依赖。
 * 2. 进阶版：面向接口编程；default 方法提供默认行为。
 *
 * 三、真实用途
 * - Spring：Repository、Service 常先定义接口再实现（便于测试 Mock）。
 * - JDK：List / Map 本身就是接口，ArrayList / HashMap 是实现。
 */

/** 支付能力契约。 */
interface Payable {
    /** @return 是否扣款成功 */
    boolean pay(int amountCents);

    /** default：接口里可提供默认实现，实现类可不覆盖。 */
    default String channelName() {
        return "unknown";
    }
}

/** 可退款能力（演示多接口）。 */
interface Refundable {
    boolean refund(int amountCents);
}

/** 抽象基类：记录已支付总额。 */
abstract class PaymentBase implements Payable {
    protected int paidTotal;

    public int getPaidTotal() {
        return paidTotal;
    }
}

/** 钱包支付：同时可退款。 */
class WalletPayment extends PaymentBase implements Refundable {
    private int balance;

    public WalletPayment(int balance) {
        this.balance = balance;
    }

    @Override
    public boolean pay(int amountCents) {
        if (amountCents <= 0 || amountCents > balance) {
            return false;
        }
        balance -= amountCents;
        paidTotal += amountCents;
        return true;
    }

    @Override
    public boolean refund(int amountCents) {
        if (amountCents <= 0 || amountCents > paidTotal) {
            return false;
        }
        balance += amountCents;
        paidTotal -= amountCents;
        return true;
    }

    @Override
    public String channelName() {
        return "wallet";
    }

    public int getBalance() {
        return balance;
    }
}

/** 仅支付、不退款的渠道示例。 */
class CouponPayment extends PaymentBase {
    private boolean used;

    @Override
    public boolean pay(int amountCents) {
        if (used || amountCents <= 0) {
            return false;
        }
        used = true;
        paidTotal += amountCents;
        return true;
    }

    @Override
    public String channelName() {
        return "coupon";
    }
}

public class L00_InterfaceAndAbstract {

    /** 面向接口：只依赖 Payable，不关心具体类。 */
    public static boolean checkout(Payable payable, int amount) {
        return payable.pay(amount);
    }

    public static void runTests() {
        WalletPayment wallet = new WalletPayment(1000);
        if (!checkout(wallet, 300)) {
            throw new AssertionError("钱包应能支付 300");
        }
        if (wallet.getBalance() != 700) {
            throw new AssertionError("余额应为 700");
        }
        if (!"wallet".equals(wallet.channelName())) {
            throw new AssertionError("渠道名应为 wallet");
        }
        if (!wallet.refund(100)) {
            throw new AssertionError("应能退 100");
        }
        if (wallet.getBalance() != 800 || wallet.getPaidTotal() != 200) {
            throw new AssertionError("退款后状态不对");
        }

        CouponPayment coupon = new CouponPayment();
        if (!checkout(coupon, 50)) {
            throw new AssertionError("优惠券首次应可用");
        }
        if (checkout(coupon, 50)) {
            throw new AssertionError("优惠券不能用两次");
        }
        // 多接口：WalletPayment 既是 Payable 又是 Refundable
        Refundable refundable = wallet;
        if (refundable.refund(9999)) {
            throw new AssertionError("超额退款应失败");
        }
        System.out.println("✅ InterfaceAndAbstract 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 InterfaceAndAbstract ************\n");
        Payable p = new WalletPayment(500);
        System.out.println(p.channelName() + " pay 100? " + p.pay(100));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
