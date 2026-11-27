import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * [主题]: 事务边界 —— @Transactional 放哪、传播直觉、失败回滚
 * [难度]: Hard
 * [前置]: 05_tx、07_repository、00_layering
 *
 * 一、主题说明
 * Spring 默认：Service public 方法上 @Transactional，经代理切入，异常回滚。
 * 坑：同类自调用不走代理；RuntimeException 默认回滚、checked 默认不回滚。
 * 本课用「事务模板」模拟代理包裹，演示下单扣库存原子性。
 *
 * 二、写法对比
 * 1. 直观版：Service 无事务，中途失败脏数据。
 * 2. 进阶版：TxTemplate.execute 包裹，失败 rollback。
 *
 * 三、真实用途
 * - 下单、支付记账、批量导入；结合隔离级别见 17_data_infra。
 */
public class L00_TransactionalBoundary {

    static class StockDb {
        final Map<String, Integer> stock = new HashMap<>();
        final Map<String, Integer> staging = new HashMap<>();
        boolean inTx;

        void begin() {
            staging.clear();
            staging.putAll(stock);
            inTx = true;
        }

        void commit() {
            stock.clear();
            stock.putAll(staging);
            inTx = false;
        }

        void rollback() {
            inTx = false;
            staging.clear();
        }

        int get(String sku) {
            Map<String, Integer> view = inTx ? staging : stock;
            return view.getOrDefault(sku, 0);
        }

        void set(String sku, int qty) {
            if (!inTx) {
                stock.put(sku, qty);
            } else {
                staging.put(sku, qty);
            }
        }
    }

    static class TxTemplate {
        private final StockDb db;

        TxTemplate(StockDb db) {
            this.db = db;
        }

        <T> T execute(Supplier<T> action) {
            db.begin();
            try {
                T result = action.get();
                db.commit();
                return result;
            } catch (RuntimeException ex) {
                db.rollback();
                throw ex;
            }
        }
    }

    static class OrderServiceNaive {
        final StockDb db;

        OrderServiceNaive(StockDb db) {
            this.db = db;
        }

        void place(String sku, int qty, boolean failAfterDeduct) {
            int left = db.get(sku);
            if (left < qty) {
                throw new IllegalStateException("库存不足");
            }
            db.set(sku, left - qty);
            if (failAfterDeduct) {
                throw new IllegalStateException("支付失败");
            }
        }
    }

    static class OrderServiceTx {
        final StockDb db;
        final TxTemplate tx;

        OrderServiceTx(StockDb db) {
            this.db = db;
            this.tx = new TxTemplate(db);
        }

        void place(String sku, int qty, boolean failAfterDeduct) {
            tx.execute(() -> {
                int left = db.get(sku);
                if (left < qty) {
                    throw new IllegalStateException("库存不足");
                }
                db.set(sku, left - qty);
                if (failAfterDeduct) {
                    throw new IllegalStateException("支付失败");
                }
                return null;
            });
        }
    }

    public static void runTests() {
        StockDb dirty = new StockDb();
        dirty.set("A", 10);
        OrderServiceNaive naive = new OrderServiceNaive(dirty);
        try {
            naive.place("A", 3, true);
        } catch (IllegalStateException ignored) {
            // expected
        }
        if (dirty.get("A") != 7) {
            throw new AssertionError("无事务失败后库存被扣成脏数据（本断言确认事故）");
        }

        StockDb safe = new StockDb();
        safe.set("A", 10);
        OrderServiceTx svc = new OrderServiceTx(safe);
        try {
            svc.place("A", 3, true);
            throw new AssertionError("应抛支付失败");
        } catch (IllegalStateException ex) {
            if (!ex.getMessage().contains("支付")) {
                throw new AssertionError("异常信息不对");
            }
        }
        if (safe.get("A") != 10) {
            throw new AssertionError("回滚后库存应仍为 10");
        }
        svc.place("A", 3, false);
        if (safe.get("A") != 7) {
            throw new AssertionError("成功提交后应为 7");
        }
        System.out.println("✅ TransactionalBoundary 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 TransactionalBoundary ************\n");
        StockDb db = new StockDb();
        db.set("sku", 5);
        new OrderServiceTx(db).place("sku", 2, false);
        System.out.println("stock=" + db.get("sku"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
