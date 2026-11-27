import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: 事务隔离级别 —— 脏读 / 不可重复读 / 幻读直觉
 * [难度]: Hard
 * [前置]: 05_tx、14_jpa_tx
 *
 * 一、主题说明
 * 隔离级别越高，异常现象越少，并发性能通常越差。
 * MySQL InnoDB 默认 REPEATABLE READ；很多业务用 READ COMMITTED 也够。
 * 本课用两连接共享「已提交视图 / 未提交缓冲」模拟 READ UNCOMMITTED vs READ COMMITTED。
 *
 * 二、写法对比
 * 1. 直观版：无隔离，读到别人未提交（脏读）。
 * 2. 进阶版：只能读已提交快照。
 *
 * 三、真实用途
 * - 对账、库存；面试必问现象与级别对应关系。
 */
public class L00_IsolationLevels {

    static class Engine {
        final Map<String, Integer> committed = new HashMap<>();
        final Map<Integer, Map<String, Integer>> uncommitted = new HashMap<>();
        int nextTx = 1;

        int begin() {
            int id = nextTx++;
            uncommitted.put(id, new HashMap<>());
            return id;
        }

        void write(int tx, String key, int value) {
            uncommitted.get(tx).put(key, value);
        }

        /** READ UNCOMMITTED：能看见任意未提交写。 */
        int readUncommitted(String key) {
            for (Map<String, Integer> buf : uncommitted.values()) {
                if (buf.containsKey(key)) {
                    return buf.get(key);
                }
            }
            return committed.getOrDefault(key, 0);
        }

        /** READ COMMITTED：只看已提交。 */
        int readCommitted(String key) {
            return committed.getOrDefault(key, 0);
        }

        void commit(int tx) {
            committed.putAll(uncommitted.remove(tx));
        }

        void rollback(int tx) {
            uncommitted.remove(tx);
        }
    }

    public static void runTests() {
        Engine e = new Engine();
        e.committed.put("bal", 100);
        int txA = e.begin();
        e.write(txA, "bal", 1); // 未提交改成 1

        if (e.readUncommitted("bal") != 1) {
            throw new AssertionError("未提交隔离应脏读到 1");
        }
        if (e.readCommitted("bal") != 100) {
            throw new AssertionError("RC 应仍读 100");
        }
        e.rollback(txA);
        if (e.readUncommitted("bal") != 100 || e.readCommitted("bal") != 100) {
            throw new AssertionError("回滚后应恢复 100");
        }

        int txB = e.begin();
        e.write(txB, "bal", 80);
        e.commit(txB);
        if (e.readCommitted("bal") != 80) {
            throw new AssertionError("提交后 RC 应读 80");
        }

        // 不可重复读演示：两次 RC 读之间被别人提交修改
        e.committed.put("price", 10);
        int r1 = e.readCommitted("price");
        int txC = e.begin();
        e.write(txC, "price", 12);
        e.commit(txC);
        int r2 = e.readCommitted("price");
        if (r1 != 10 || r2 != 12) {
            throw new AssertionError("RC 下不可重复读: " + r1 + " -> " + r2);
        }
        System.out.println("✅ IsolationLevels 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 IsolationLevels ************\n");
        Engine e = new Engine();
        e.committed.put("x", 5);
        int tx = e.begin();
        e.write(tx, "x", 9);
        System.out.println("RU=" + e.readUncommitted("x") + " RC=" + e.readCommitted("x"));
        e.commit(tx);
        System.out.println("after commit RC=" + e.readCommitted("x"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
