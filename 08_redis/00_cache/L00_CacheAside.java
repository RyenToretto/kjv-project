import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: Redis 缓存旁路（Cache-Aside）读写模式
 * [难度]: Medium
 * [前置]: 06_spring_boot、07_persistence
 *
 * 一、主题说明
 * 缓存旁路：读 —— 先查缓存，未命中再查 DB 并回填；写 —— 先更新 DB，再删缓存（或更新缓存）。
 * 本课用内存 Map 模拟 Redis，建立模式直觉（真 Redis 命令：GET/SET/DEL）。
 *
 * 二、写法对比
 * 1. 直观版：每次都打 DB。
 * 2. 进阶版：Cache-Aside，统计 DB 访问次数。
 *
 * 三、真实用途
 * - 商品详情、用户会话、热点配置；注意缓存穿透/击穿/雪崩（下一课过期）。
 */
public class L00_CacheAside {

    static class FakeRedis {
        final Map<String, String> kv = new HashMap<>();

        String get(String key) {
            return kv.get(key);
        }

        void set(String key, String value) {
            kv.put(key, value);
        }

        void del(String key) {
            kv.remove(key);
        }
    }

    static class FakeDb {
        final Map<String, String> rows = new HashMap<>();
        final AtomicInteger reads = new AtomicInteger();
        final AtomicInteger writes = new AtomicInteger();

        String select(String id) {
            reads.incrementAndGet();
            return rows.get(id);
        }

        void update(String id, String value) {
            writes.incrementAndGet();
            rows.put(id, value);
        }
    }

    public static String getNaive(FakeDb db, String id) {
        return db.select(id);
    }

    public static String getCacheAside(FakeRedis redis, FakeDb db, String id) {
        String key = "user:" + id;
        String cached = redis.get(key);
        if (cached != null) {
            return cached;
        }
        String fromDb = db.select(id);
        if (fromDb != null) {
            redis.set(key, fromDb);
        }
        return fromDb;
    }

    public static void updateCacheAside(FakeRedis redis, FakeDb db, String id, String value) {
        db.update(id, value);
        redis.del("user:" + id); // 删缓存，下次读回填
    }

    public static void runTests() {
        FakeDb db = new FakeDb();
        db.rows.put("1", "alice");
        FakeRedis redis = new FakeRedis();

        if (!"alice".equals(getCacheAside(redis, db, "1"))) {
            throw new AssertionError("首次读应来自 DB");
        }
        if (!"alice".equals(getCacheAside(redis, db, "1"))) {
            throw new AssertionError("二次应命中缓存");
        }
        if (db.reads.get() != 1) {
            throw new AssertionError("DB 只应读 1 次, 实际=" + db.reads.get());
        }

        updateCacheAside(redis, db, "1", "alice2");
        if (!"alice2".equals(getCacheAside(redis, db, "1"))) {
            throw new AssertionError("更新后应读到新值");
        }
        if (db.reads.get() != 2) {
            throw new AssertionError("删缓存后应再打一次 DB");
        }

        // 对照：无缓存每次都打 DB
        FakeDb db2 = new FakeDb();
        db2.rows.put("1", "x");
        getNaive(db2, "1");
        getNaive(db2, "1");
        if (db2.reads.get() != 2) {
            throw new AssertionError("无缓存应每次读 DB");
        }
        System.out.println("✅ CacheAside 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CacheAside ************\n");
        FakeDb db = new FakeDb();
        db.rows.put("9", "bob");
        FakeRedis redis = new FakeRedis();
        System.out.println(getCacheAside(redis, db, "9"));
        System.out.println(getCacheAside(redis, db, "9") + " dbReads=" + db.reads.get());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
