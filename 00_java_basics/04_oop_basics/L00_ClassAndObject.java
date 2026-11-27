/**
 * [主题]: 类与对象、封装（字段 private + getter/setter）
 * [难度]: Easy
 * [前置]: 03_arrays_strings
 *
 * 一、主题说明
 * 类（class）是模板；对象（object / instance）是按模板 new 出来的具体东西。
 * 封装：把字段藏起来（private），只通过方法读写，便于校验与改实现。
 *
 * 二、写法对比
 * 1. 直观版：字段 public，外部随便改（入门能跑，但不推荐）。
 * 2. 进阶版：private 字段 + 构造器 + getter；非法值在构造/setter 拒绝。
 *
 * 三、真实用途
 * - 领域模型：User、Order、Product 都是类。
 * - Spring 里实体、DTO、配置属性本质都是「带数据的对象」。
 */

/** 直观版：公开字段（对比用，生产勿模仿）。 */
class NaiveAccount {
    public String owner;
    public int balance; // 单位：分，可为负（坏处：无保护）
}

/** 进阶版：封装后的账户。 */
class Account {
    private final String owner;
    private int balanceCents;

    public Account(String owner, int balanceCents) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner 不能为空");
        }
        if (balanceCents < 0) {
            throw new IllegalArgumentException("初始余额不能为负");
        }
        this.owner = owner.trim();
        this.balanceCents = balanceCents;
    }

    public String getOwner() {
        return owner;
    }

    public int getBalanceCents() {
        return balanceCents;
    }

    /** 存款：金额必须为正。 */
    public void deposit(int cents) {
        if (cents <= 0) {
            throw new IllegalArgumentException("存款金额必须 > 0");
        }
        balanceCents += cents;
    }

    /** 取款：余额不足则失败。 */
    public boolean withdraw(int cents) {
        if (cents <= 0) {
            throw new IllegalArgumentException("取款金额必须 > 0");
        }
        if (cents > balanceCents) {
            return false;
        }
        balanceCents -= cents;
        return true;
    }
}

public class L00_ClassAndObject {

    public static int naiveDeposit(NaiveAccount acc, int cents) {
        acc.balance += cents; // 无校验，可为负额「存款」
        return acc.balance;
    }

    public static void runTests() {
        NaiveAccount naive = new NaiveAccount();
        naive.owner = "tom";
        naive.balance = 100;
        if (naiveDeposit(naive, 50) != 150) {
            throw new AssertionError("直观版存款失败");
        }

        Account acc = new Account("alice", 1000);
        if (!"alice".equals(acc.getOwner())) {
            throw new AssertionError("owner 应为 alice");
        }
        acc.deposit(200);
        if (acc.getBalanceCents() != 1200) {
            throw new AssertionError("存款后应为 1200");
        }
        if (!acc.withdraw(200)) {
            throw new AssertionError("应能取出 200");
        }
        if (acc.withdraw(99999)) {
            throw new AssertionError("超额取款应失败");
        }
        if (acc.getBalanceCents() != 1000) {
            throw new AssertionError("失败取款后余额应不变");
        }

        boolean badOwner = false;
        try {
            new Account("  ", 0);
        } catch (IllegalArgumentException ex) {
            badOwner = true;
        }
        if (!badOwner) {
            throw new AssertionError("空 owner 应抛异常");
        }
        System.out.println("✅ ClassAndObject 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ClassAndObject ************\n");
        Account acc = new Account("bob", 500);
        acc.deposit(100);
        System.out.println(acc.getOwner() + " 余额(分)=" + acc.getBalanceCents());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
