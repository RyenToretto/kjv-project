import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * [主题]: 分布式锁入门 —— SET NX + 持有者校验释放
 * [难度]: Medium～Hard
 * [前置]: 00_cache
 *
 * 一、主题说明
 * 多实例部署时，JVM 内 synchronized 挡不住别的进程。
 * Redis 锁直觉：SET key token NX EX ttl；释放时校验 token，避免误删别人的锁。
 * 生产还有 Redisson、续期看门狗、红锁争议等，本课只建最小正确模型。
 *
 * 二、写法对比
 * 1. 直观版：无 token，谁都能 DEL（危险）。
 * 2. 进阶版：UUID token + 比对后删除。
 *
 * 三、真实用途
 * - 定时任务防重、秒杀扣减、幂等临界区。
 */
public class L00_DistributedLockIntro {

    static class FakeRedisLock {
        final Map<String, String> kv = new HashMap<>();

        synchronized boolean tryLock(String key, String token, long ttlIgnored) {
            if (kv.containsKey(key)) {
                return false;
            }
            kv.put(key, token);
            return true;
        }

        /** 危险：不校验持有者。 */
        synchronized void unlockNaive(String key) {
            kv.remove(key);
        }

        synchronized boolean unlockBetter(String key, String token) {
            String cur = kv.get(key);
            if (token != null && token.equals(cur)) {
                kv.remove(key);
                return true;
            }
            return false;
        }
    }

    public static String newToken() {
        return UUID.randomUUID().toString();
    }

    public static void runTests() {
        FakeRedisLock redis = new FakeRedisLock();
        String t1 = "token-1";
        String t2 = "token-2";
        if (!redis.tryLock("order:1", t1, 3000)) {
            throw new AssertionError("首次加锁应成功");
        }
        if (redis.tryLock("order:1", t2, 3000)) {
            throw new AssertionError("重复加锁应失败");
        }
        // 别人用 naive 解锁会误伤
        redis.unlockNaive("order:1");
        if (!redis.tryLock("order:1", t2, 3000)) {
            throw new AssertionError("naive 解锁后应能再锁");
        }
        // better：错误 token 不能解锁
        if (redis.unlockBetter("order:1", t1)) {
            throw new AssertionError("错误 token 不应解锁");
        }
        if (!redis.unlockBetter("order:1", t2)) {
            throw new AssertionError("正确 token 应解锁");
        }
        if (!redis.tryLock("order:1", newToken(), 3000)) {
            throw new AssertionError("释放后应可再获取");
        }
        System.out.println("✅ DistributedLockIntro 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DistributedLockIntro ************\n");
        FakeRedisLock redis = new FakeRedisLock();
        String token = newToken();
        System.out.println("lock? " + redis.tryLock("job", token, 1000));
        System.out.println("unlock? " + redis.unlockBetter("job", token));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
