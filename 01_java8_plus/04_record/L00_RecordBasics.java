import java.util.Objects;

/**
 * [主题]: Record —— 简洁的不可变数据载体（Java 16+，按 25 教学）
 * [难度]: Easy
 * [前置]: 04_oop_basics
 *
 * 一、主题说明
 * record 自动生成：final 字段、构造器、equals / hashCode / toString、访问器（name() 而非 getName）。
 * 适合：DTO、值对象、Map 的复合键；不适合：需要继承层级的富领域模型（record 不能 extends 其它类）。
 *
 * 二、写法对比
 * 1. 直观版：手写 POJO（样板代码多）。
 * 2. 进阶版：record；紧凑构造器做校验。
 *
 * 三、真实用途
 * - REST 请求/响应体；配置项；方法多返回值打包。
 * - switch 模式匹配可与 record 搭配（进阶）。
 */

/** 直观版 POJO。 */
class MoneyPojo {
    private final int cents;
    private final String currency;

    MoneyPojo(int cents, String currency) {
        this.cents = cents;
        this.currency = currency;
    }

    public int getCents() {
        return cents;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoneyPojo that)) {
            return false;
        }
        return cents == that.cents && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cents, currency);
    }
}

/** 进阶版 record。 */
record Money(int cents, String currency) {
    Money {
        // 紧凑构造器：校验后赋值由编译器完成
        if (cents < 0) {
            throw new IllegalArgumentException("cents 不能为负");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency 必填");
        }
        currency = currency.trim().toUpperCase();
    }

    public Money plus(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不一致");
        }
        return new Money(cents + other.cents, currency);
    }
}

/** 订单行：嵌套 record。 */
record OrderLine(String sku, int qty, Money price) {
    int lineTotalCents() {
        return price.cents() * qty;
    }
}

public class L00_RecordBasics {

    public static void runTests() {
        MoneyPojo p1 = new MoneyPojo(100, "CNY");
        MoneyPojo p2 = new MoneyPojo(100, "CNY");
        if (!p1.equals(p2)) {
            throw new AssertionError("POJO equals 失败");
        }

        Money m1 = new Money(100, "cny");
        Money m2 = new Money(100, "CNY");
        if (!m1.equals(m2)) {
            throw new AssertionError("record 规范化后应相等");
        }
        if (!"CNY".equals(m1.currency())) {
            throw new AssertionError("currency 应大写");
        }
        Money sum = m1.plus(new Money(50, "CNY"));
        if (sum.cents() != 150) {
            throw new AssertionError("plus 应为 150");
        }

        boolean negative = false;
        try {
            new Money(-1, "CNY");
        } catch (IllegalArgumentException ex) {
            negative = true;
        }
        if (!negative) {
            throw new AssertionError("负金额应失败");
        }

        OrderLine line = new OrderLine("SKU-1", 3, new Money(200, "CNY"));
        if (line.lineTotalCents() != 600) {
            throw new AssertionError("行合计应为 600");
        }
        // toString 自动生成，至少包含类型名
        if (!m1.toString().contains("Money")) {
            throw new AssertionError("toString 应含 Money");
        }
        System.out.println("✅ RecordBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 RecordBasics ************\n");
        Money m = new Money(1999, "cny");
        System.out.println(m);
        System.out.println("访问器 cents()=" + m.cents());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
