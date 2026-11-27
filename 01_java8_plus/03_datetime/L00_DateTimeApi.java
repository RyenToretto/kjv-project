import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * [主题]: java.time 日期时间 API（替代 Date / Calendar）
 * [难度]: Easy～Medium
 * [前置]: 00_java_basics
 *
 * 一、主题说明
 * LocalDate / LocalDateTime：无时区的日期时间（业务「日历日」常用）。
 * ZonedDateTime：带时区；跨地区系统要用。
 * 不可变：plusDays 等返回新对象，旧对象不变。
 *
 * 二、写法对比
 * 1. 直观版：手动算天数差（易错，忽略闰年等）。
 * 2. 进阶版：ChronoUnit / Duration / DateTimeFormatter。
 *
 * 三、真实用途
 * - 订单创建时间、优惠券有效期、对账日切。
 * - 日志与 API 统一 ISO-8601 格式。
 */
public class L00_DateTimeApi {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 直观版：只适用于「简单同月」演示，故意简陋。 */
    public static long daysBetweenNaive(LocalDate from, LocalDate to) {
        // 仅当同月时「碰巧」正确；跨月会错——用来对比 API
        if (from.getYear() == to.getYear() && from.getMonth() == to.getMonth()) {
            return to.getDayOfMonth() - from.getDayOfMonth();
        }
        // 退化：交给进阶版，避免课里写错日历算法
        return ChronoUnit.DAYS.between(from, to);
    }

    /** 进阶版：ChronoUnit 计算整天数。 */
    public static long daysBetweenBetter(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static String formatDay(LocalDate day) {
        return day.format(DAY_FMT);
    }

    public static LocalDate parseDay(String text) {
        return LocalDate.parse(text, DAY_FMT);
    }

    /** 优惠券：从 now 起有效 days 天（含起始日则 plusDays(days) 为截止次日 0 点直觉，这里用日期 inclusive）。 */
    public static boolean isCouponValid(LocalDate start, int validDays, LocalDate today) {
        LocalDate endInclusive = start.plusDays(validDays - 1L);
        return !today.isBefore(start) && !today.isAfter(endInclusive);
    }

    /** Duration：两时刻相差的分钟数。 */
    public static long minutesBetween(LocalDateTime a, LocalDateTime b) {
        return Duration.between(a, b).toMinutes();
    }

    /** 时区转换：同一瞬间在上海与纽约的本地时间不同。 */
    public static String shanghaiAndNewYork(LocalDateTime localShanghai) {
        ZonedDateTime sh = localShanghai.atZone(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime ny = sh.withZoneSameInstant(ZoneId.of("America/New_York"));
        return sh.toLocalDateTime() + "|" + ny.toLocalDateTime();
    }

    public static void runTests() {
        LocalDate a = LocalDate.of(2026, 1, 1);
        LocalDate b = LocalDate.of(2026, 1, 11);
        if (daysBetweenBetter(a, b) != 10) {
            throw new AssertionError("1/1 到 1/11 应为 10 天");
        }
        if (daysBetweenNaive(a, b) != 10) {
            throw new AssertionError("同月直观版应为 10");
        }
        LocalDate c = LocalDate.of(2026, 2, 1);
        if (daysBetweenBetter(a, c) != 31) {
            throw new AssertionError("跨月应为 31");
        }
        if (!"2026-11-27".equals(formatDay(LocalDate.of(2026, 11, 27)))) {
            throw new AssertionError("格式化失败");
        }
        if (!parseDay("2026-11-27").equals(LocalDate.of(2026, 11, 27))) {
            throw new AssertionError("解析失败");
        }
        if (!isCouponValid(LocalDate.of(2026, 1, 1), 3, LocalDate.of(2026, 1, 3))) {
            throw new AssertionError("第 3 天应有效");
        }
        if (isCouponValid(LocalDate.of(2026, 1, 1), 3, LocalDate.of(2026, 1, 4))) {
            throw new AssertionError("第 4 天应失效");
        }
        LocalDateTime t1 = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 1, 1, 11, 30);
        if (minutesBetween(t1, t2) != 90) {
            throw new AssertionError("应为 90 分钟");
        }
        String zones = shanghaiAndNewYork(LocalDateTime.of(2026, 6, 1, 12, 0));
        if (!zones.contains("|")) {
            throw new AssertionError("时区串应含分隔符");
        }
        System.out.println("✅ DateTimeApi 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DateTimeApi ************\n");
        System.out.println("今天格式化: " + formatDay(LocalDate.of(2026, 11, 27)));
        System.out.println("时区演示: " + shanghaiAndNewYork(LocalDateTime.of(2026, 6, 1, 12, 0)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
