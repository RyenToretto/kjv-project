/**
 * [主题]: 继承与多态
 * [难度]: Easy～Medium
 * [前置]: 04_oop_basics
 *
 * 一、主题说明
 * 继承（extends）：子类复用父类字段/方法，并可以覆盖（override）行为。
 * 多态：父类型引用指向子类对象，调用方法时走子类实现（运行时分派）。
 * Java 单继承：一个类只能 extends 一个父类；多能力用接口（下一课）。
 *
 * 二、写法对比
 * 1. 直观版：if 判断类型再算运费（每加一种快递就改一堆 if）。
 * 2. 进阶版：继承 + 多态，新增快递类型只需新子类。
 *
 * 三、真实用途
 * - 支付渠道、通知渠道、策略算法的基类扩展。
 * - 但「为了继承而继承」会僵化；很多场景接口 + 组合更合适（后续模式课）。
 */

/** 父类：运费计算器的公共部分。 */
abstract class Shipping {
    protected final String name;

    protected Shipping(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** 子类必须实现：按重量(kg)算运费（单位：分）。 */
    public abstract int feeCents(int weightKg);
}

class StandardShipping extends Shipping {
    public StandardShipping() {
        super("standard");
    }

    @Override
    public int feeCents(int weightKg) {
        return 500 + weightKg * 100; // 5 元起步 + 1 元/kg
    }
}

class ExpressShipping extends Shipping {
    public ExpressShipping() {
        super("express");
    }

    @Override
    public int feeCents(int weightKg) {
        return 1200 + weightKg * 200;
    }
}

public class L00_InheritancePolymorphism {

    /** 直观版：字符串类型 + if。 */
    public static int feeNaive(String type, int weightKg) {
        if ("standard".equals(type)) {
            return 500 + weightKg * 100;
        } else if ("express".equals(type)) {
            return 1200 + weightKg * 200;
        }
        throw new IllegalArgumentException("未知类型: " + type);
    }

    /** 进阶版：多态，调用方只依赖 Shipping。 */
    public static int feeBetter(Shipping shipping, int weightKg) {
        return shipping.feeCents(weightKg);
    }

    /** 批量：多态列表汇总。 */
    public static int totalFees(Shipping[] list, int weightKg) {
        int sum = 0;
        for (Shipping s : list) {
            sum += s.feeCents(weightKg);
        }
        return sum;
    }

    public static void runTests() {
        if (feeNaive("standard", 2) != 700) {
            throw new AssertionError("标准 2kg 应为 700");
        }
        if (feeNaive("express", 2) != 1600) {
            throw new AssertionError("加急 2kg 应为 1600");
        }
        Shipping std = new StandardShipping();
        Shipping exp = new ExpressShipping();
        if (feeBetter(std, 2) != feeNaive("standard", 2)) {
            throw new AssertionError("多态标准运费应与直观版一致");
        }
        if (feeBetter(exp, 2) != feeNaive("express", 2)) {
            throw new AssertionError("多态加急运费应与直观版一致");
        }
        if (!"standard".equals(std.getName())) {
            throw new AssertionError("名称应为 standard");
        }
        if (totalFees(new Shipping[]{std, exp}, 1) != (600 + 1400)) {
            throw new AssertionError("批量运费汇总错误");
        }
        // 覆盖：子类方法确实被调用（express 明显更贵）
        if (feeBetter(exp, 1) <= feeBetter(std, 1)) {
            throw new AssertionError("加急应比标准贵");
        }
        System.out.println("✅ InheritancePolymorphism 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 InheritancePolymorphism ************\n");
        Shipping s = new ExpressShipping(); // 父类型引用
        System.out.println(s.getName() + " 3kg 运费(分)=" + s.feeCents(3));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
