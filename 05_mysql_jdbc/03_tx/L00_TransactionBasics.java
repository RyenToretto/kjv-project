import java.util.HashMap;
import java.util.Map;

/**
 * [主题]: 事务 —— 原子性、提交与回滚（内存账户模拟）
 * [难度]: Medium
 * [前置]: 02_jdbc
 *
 * 一、主题说明
 * 事务把多步写操作捆成「要么全成功，要么全失败」。
 * ACID：原子性 / 一致性 / 隔离性 / 持久性。本课聚焦原子性：转账扣 A 加 B。
 * JDBC：conn.setAutoCommit(false); 成功 commit，异常 rollback。
 *
 * 二、写法对比
 * 1. 直观版：两步更新，中途失败导致脏数据。
 * 2. 进阶版：事务缓冲区，失败回滚到快照。
 *
 * 三、真实用途
 * - 下单扣库存、支付记账、余额转账。
 * - 隔离级别（读已提交等）后续结合真 MySQL 再练。
 */
public class L00_TransactionBasics {

    static class Ledger {
        final Map<String, Integer> balances = new HashMap<>();

        Ledger put(String id, int cents) {
            balances.put(id, cents);
            return this;
        }

        int get(String id) {
            return balances.getOrDefault(id, 0);
        }
    }

    /** 无事务：先扣后加，若加失败则钱消失。 */
    public static boolean transferNaive(Ledger ledger, String from, String to, int amount, boolean failAfterDebit) {
        if (ledger.get(from) < amount) {
            return false;
        }
        ledger.balances.put(from, ledger.get(from) - amount);
        if (failAfterDebit) {
            return false; // 已扣款却没加款 —— 典型事故
        }
        ledger.balances.put(to, ledger.get(to) + amount);
        return true;
    }

    /** 有事务：变更先写入缓冲，commit 才落地；rollback 丢弃。 */
    public static class TxLedger {
        private final Ledger ledger;
        private Map<String, Integer> staging;
        private boolean inTx;

        TxLedger(Ledger ledger) {
            this.ledger = ledger;
        }

        void begin() {
            staging = new HashMap<>(ledger.balances);
            inTx = true;
        }

        void debit(String id, int amount) {
            ensureTx();
            int bal = staging.getOrDefault(id, 0);
            if (bal < amount) {
                throw new IllegalStateException("余额不足");
            }
            staging.put(id, bal - amount);
        }

        void credit(String id, int amount) {
            ensureTx();
            staging.put(id, staging.getOrDefault(id, 0) + amount);
        }

        void commit() {
            ensureTx();
            ledger.balances.clear();
            ledger.balances.putAll(staging);
            inTx = false;
            staging = null;
        }

        void rollback() {
            ensureTx();
            inTx = false;
            staging = null;
        }

        private void ensureTx() {
            if (!inTx) {
                throw new IllegalStateException("不在事务中");
            }
        }
    }

    public static boolean transferBetter(Ledger ledger, String from, String to, int amount, boolean failAfterDebit) {
        TxLedger tx = new TxLedger(ledger);
        tx.begin();
        try {
            tx.debit(from, amount);
            if (failAfterDebit) {
                throw new IllegalStateException("模拟下游失败");
            }
            tx.credit(to, amount);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            tx.rollback();
            return false;
        }
    }

    public static void runTests() {
        Ledger dirty = new Ledger().put("A", 1000).put("B", 0);
        transferNaive(dirty, "A", "B", 300, true);
        if (dirty.get("A") != 700 || dirty.get("B") != 0) {
            throw new AssertionError("无事务失败后出现不一致（本断言确认事故形态）");
        }

        Ledger safe = new Ledger().put("A", 1000).put("B", 0);
        if (transferBetter(safe, "A", "B", 300, true)) {
            throw new AssertionError("失败转账应返回 false");
        }
        if (safe.get("A") != 1000 || safe.get("B") != 0) {
            throw new AssertionError("回滚后余额应还原");
        }
        if (!transferBetter(safe, "A", "B", 300, false)) {
            throw new AssertionError("成功转账应 true");
        }
        if (safe.get("A") != 700 || safe.get("B") != 300) {
            throw new AssertionError("成功后 A=700 B=300");
        }
        System.out.println("✅ TransactionBasics 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 TransactionBasics ************\n");
        Ledger ledger = new Ledger().put("A", 500).put("B", 100);
        System.out.println("transfer ok? " + transferBetter(ledger, "A", "B", 50, false));
        System.out.println("A=" + ledger.get("A") + " B=" + ledger.get("B"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
