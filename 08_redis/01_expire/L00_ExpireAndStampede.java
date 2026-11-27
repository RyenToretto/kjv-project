import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * [主题]: 过期时间、缓存穿透与雪崩直觉
 * [难度]: Medium
 * [前置]: 00_cache
 *
 * 一、主题说明
 * TTL：键到期自动删除。雪崩：大量键同一时刻过期 → 瞬间打爆 DB。
 * 穿透：查不存在的数据，缓存与 DB 都没有，每次打穿到 DB。
 * 对策直觉：空值短 TTL、布隆过滤（进阶）、过期加随机抖动。
 *
 * 二、写法对比
 * 1. 直观版：固定 TTL，易齐刷。
 * 2. 进阶版：TTL + 随机抖动；缓存空值防穿透。
 *
 * 三、真实用途
 * - 热点 key、活动结束瞬间流量；生产监控缓存命中率。
 */
public class L00_ExpireAndStampede {

    static class Entry {
        final String value;
        final long expireAtMs;

        Entry(String value, long expireAtMs) {
            this.value = value;
            this.expireAtMs = expireAtMs;
        }

        boolean expired(long now) {
            return now >= expireAtMs;
        }
    }

    static class TtlCache {
        final Map<String, Entry> map = new HashMap<>();

        void set(String key, String value, long ttlMs, long now) {
            map.put(key, new Entry(value, now + ttlMs));
        }

        String get(String key, long now) {
            Entry e = map.get(key);
            if (e == null || e.expired(now)) {
                map.remove(key);
                return null;
            }
            return e.value;
        }
    }

    /** 固定 TTL：同一批 key 会在同一时刻过期。 */
    public static long fixedExpireAt(long now, long ttlMs) {
        return now + ttlMs;
    }

    /** 抖动：ttl ± jitter，打散过期点。 */
    public static long jitterExpireAt(long now, long ttlMs, long jitterMs, long randomInJitter) {
        long delta = Math.floorMod(randomInJitter, jitterMs + 1);
        return now + ttlMs + delta;
    }

    public static void cacheNullForMiss(TtlCache cache, String key, long now, long nullTtl) {
        cache.set(key, "__NULL__", nullTtl, now);
    }

    public static boolean isCachedNull(String value) {
        return "__NULL__".equals(value);
    }

    public static void runTests() {
        long now = 1_000_000L;
        TtlCache cache = new TtlCache();
        cache.set("a", "1", 100, now);
        if (!"1".equals(cache.get("a", now + 50))) {
            throw new AssertionError("未过期应命中");
        }
        if (cache.get("a", now + 100) != null) {
            throw new AssertionError("到期应 miss");
        }

        long e1 = fixedExpireAt(now, 1000);
        long e2 = jitterExpireAt(now, 1000, 200, 150);
        if (e1 != now + 1000) {
            throw new AssertionError("固定过期错误");
        }
        if (e2 != now + 1000 + 150) {
            throw new AssertionError("抖动过期错误");
        }
        if (e1 == e2) {
            throw new AssertionError("抖动应与固定不同（本用例 random=150）");
        }

        cacheNullForMiss(cache, "missing", now, 30);
        String v = cache.get("missing", now + 1);
        if (!isCachedNull(v)) {
            throw new AssertionError("应缓存空值标记");
        }
        if (cache.get("missing", now + 30) != null) {
            throw new AssertionError("空值短 TTL 后应消失");
        }

        // 演示随机 API 可调用（不断言具体值）
        long sample = jitterExpireAt(now, 1000, 50, ThreadLocalRandom.current().nextLong(0, 51));
        if (sample < now + 1000 || sample > now + 1050) {
            throw new AssertionError("抖动范围应在 [ttl, ttl+jitter]");
        }
        System.out.println("✅ ExpireAndStampede 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ExpireAndStampede ************\n");
        System.out.println("jitter example=" + jitterExpireAt(0, 1000, 100, 42));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
