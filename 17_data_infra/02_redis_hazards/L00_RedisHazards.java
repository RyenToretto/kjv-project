import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: Redis 三大隐患 —— 穿透 / 击穿 / 雪崩（可运行对策）
 * [难度]: Hard
 * [前置]: 08_redis
 *
 * 一、主题说明
 * - 穿透：查不存在的 key，缓存与 DB 都没有 → 每次打穿 DB。对策：缓存空值 / 布隆过滤。
 * - 击穿：热点 key 过期瞬间大量并发打 DB。对策：互斥重建 / 逻辑过期。
 * - 雪崩：大量 key 同时过期。对策：TTL 抖动（见 08）。
 *
 * 二、写法对比
 * 1. 直观版：无防护，统计 DB 被打次数。
 * 2. 进阶版：空值缓存 + 单飞（singleflight）重建。
 *
 * 三、真实用途
 * - 大促商品详情、登录态；监控缓存命中率与 DB QPS。
 */
public class L00_RedisHazards {

    static class Db {
        final Map<String, String> data = new HashMap<>();
        final AtomicInteger hits = new AtomicInteger();

        String find(String id) {
            hits.incrementAndGet();
            return data.get(id);
        }
    }

    static class Cache {
        final Map<String, String> kv = new HashMap<>();

        String get(String k) {
            return kv.get(k);
        }

        void set(String k, String v) {
            kv.put(k, v);
        }
    }

    /** 穿透：无防护。 */
    public static String getPenetrating(Cache c, Db db, String id) {
        String v = c.get(id);
        if (v != null) {
            return v;
        }
        return db.find(id); // null 也不缓存 → 每次打 DB
    }

    /** 防穿透：缓存空值标记。 */
    public static String getWithNullCache(Cache c, Db db, String id) {
        String v = c.get(id);
        if (v != null) {
            return "__NULL__".equals(v) ? null : v;
        }
        String fromDb = db.find(id);
        c.set(id, fromDb == null ? "__NULL__" : fromDb);
        return fromDb;
    }

    /**
     * 防击穿：同一 key 只用一个线程回源（单飞）。
     * 简化：用 Set 标记 building。
     */
    public static String getSingleFlight(Cache c, Db db, Set<String> building, String id)
            throws InterruptedException {
        String v = c.get(id);
        if (v != null) {
            return v;
        }
        synchronized (building) {
            v = c.get(id);
            if (v != null) {
                return v;
            }
            if (!building.add(id)) {
                // 已有人在建：自旋等待（教学简化）
                while (c.get(id) == null) {
                    building.wait(10);
                }
                return c.get(id);
            }
        }
        try {
            String fromDb = db.find(id);
            String store = fromDb == null ? "__NULL__" : fromDb;
            c.set(id, store);
            return fromDb;
        } finally {
            synchronized (building) {
                building.remove(id);
                building.notifyAll();
            }
        }
    }

    public static void runTests() throws Exception {
        Db db = new Db();
        Cache c1 = new Cache();
        getPenetrating(c1, db, "nope");
        getPenetrating(c1, db, "nope");
        getPenetrating(c1, db, "nope");
        if (db.hits.get() != 3) {
            throw new AssertionError("穿透应打 DB 3 次");
        }

        Db db2 = new Db();
        Cache c2 = new Cache();
        getWithNullCache(c2, db2, "nope");
        getWithNullCache(c2, db2, "nope");
        getWithNullCache(c2, db2, "nope");
        if (db2.hits.get() != 1) {
            throw new AssertionError("空值缓存后 DB 只 1 次, 实际=" + db2.hits.get());
        }

        Db db3 = new Db();
        db3.data.put("hot", "v");
        Cache c3 = new Cache();
        Set<String> building = new HashSet<>();
        String a = getSingleFlight(c3, db3, building, "hot");
        String b = getSingleFlight(c3, db3, building, "hot");
        if (!"v".equals(a) || !"v".equals(b) || db3.hits.get() != 1) {
            throw new AssertionError("单飞回源应只打 1 次 DB");
        }
        System.out.println("✅ RedisHazards 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 RedisHazards ************\n");
        try {
            runTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
