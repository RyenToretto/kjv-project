import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * [主题]: 综合小项目 —— 迷你商店（鉴权 + CRUD + 缓存 + 消息幂等）
 * [难度]: Hard
 * [前置]: 06_～12_ 主线课
 *
 * 一、主题说明
 * 把前面阶段串起来：注册登录发 token、商品仓储、缓存旁路读、下单发消息且消费者幂等。
 * 纯 JDK 可跑，作为作品集「逻辑骨架」；真实简历项目再换成 Spring Boot + MySQL + Redis。
 *
 * 二、写法对比
 * 1. 直观版：全写在 main 里（本课避免）。
 * 2. 进阶版：分层 Auth / Catalog / Order / Cache / MQ。
 *
 * 三、真实用途
 * - 就业作品集最小闭环；面试可画架构图对照本课类。
 */
public class L00_MiniShopCapstone {

    // ---- 领域 ----
    record User(long id, String username, String password) {}

    record Product(long id, String name, int priceCents, int stock) {
        Product withStock(int s) {
            return new Product(id, name, priceCents, s);
        }
    }

    record Order(String id, long userId, long productId, int qty) {}

    // ---- 基础设施替身 ----
    static class TokenStore {
        private final Map<String, Long> tokenToUser = new HashMap<>();

        String issue(long userId) {
            String t = UUID.randomUUID().toString();
            tokenToUser.put(t, userId);
            return t;
        }

        Optional<Long> userId(String token) {
            return Optional.ofNullable(tokenToUser.get(token));
        }
    }

    static class Cache {
        private final Map<String, Product> map = new HashMap<>();

        Product get(long id) {
            return map.get("p:" + id);
        }

        void set(Product p) {
            map.put("p:" + p.id(), p);
        }

        void evict(long id) {
            map.remove("p:" + id);
        }
    }

    static class ProductRepo {
        private final AtomicLong seq = new AtomicLong(1);
        private final Map<Long, Product> db = new HashMap<>();

        Product save(Product p) {
            if (p.id() == 0) {
                long id = seq.getAndIncrement();
                Product created = new Product(id, p.name(), p.priceCents(), p.stock());
                db.put(id, created);
                return created;
            }
            db.put(p.id(), p);
            return p;
        }

        Optional<Product> find(long id) {
            return Optional.ofNullable(db.get(id));
        }
    }

    static class UserRepo {
        private final AtomicLong seq = new AtomicLong(1);
        private final Map<String, User> byName = new HashMap<>();

        User register(String username, String password) {
            if (byName.containsKey(username)) {
                throw new IllegalArgumentException("用户已存在");
            }
            User u = new User(seq.getAndIncrement(), username, password);
            byName.put(username, u);
            return u;
        }

        Optional<User> find(String username) {
            return Optional.ofNullable(byName.get(username));
        }
    }

    static class OrderMq {
        private final Set<String> processed = new HashSet<>();
        int handled;

        boolean onOrderCreated(String messageId, Runnable action) {
            if (!processed.add(messageId)) {
                return false;
            }
            action.run();
            handled++;
            return true;
        }
    }

    // ---- 应用服务 ----
    static class ShopApp {
        final UserRepo users = new UserRepo();
        final ProductRepo products = new ProductRepo();
        final TokenStore tokens = new TokenStore();
        final Cache cache = new Cache();
        final OrderMq mq = new OrderMq();
        final StringBuilder audit = new StringBuilder();

        String registerAndLogin(String username, String password) {
            users.register(username, password);
            return login(username, password);
        }

        String login(String username, String password) {
            User u = users.find(username).orElseThrow(() -> new IllegalArgumentException("无用户"));
            if (!u.password().equals(password)) {
                throw new IllegalArgumentException("密码错误");
            }
            return tokens.issue(u.id());
        }

        Product createProduct(String token, String name, int price, int stock) {
            auth(token);
            Product p = products.save(new Product(0, name, price, stock));
            cache.set(p);
            return p;
        }

        Product getProduct(long id) {
            Product cached = cache.get(id);
            if (cached != null) {
                return cached;
            }
            Product p = products.find(id).orElseThrow(() -> new IllegalArgumentException("无商品"));
            cache.set(p);
            return p;
        }

        Order placeOrder(String token, long productId, int qty) {
            long userId = auth(token);
            Product p = getProduct(productId);
            if (p.stock() < qty) {
                throw new IllegalStateException("库存不足");
            }
            Product updated = products.save(p.withStock(p.stock() - qty));
            cache.evict(productId);
            cache.set(updated);
            Order order = new Order("O-" + UUID.randomUUID(), userId, productId, qty);
            String msgId = order.id();
            mq.onOrderCreated(msgId, () -> audit.append("NOTIFY:").append(order.id()).append(';'));
            // 模拟至少一次：再投一次应幂等
            mq.onOrderCreated(msgId, () -> audit.append("DUP;"));
            return order;
        }

        private long auth(String token) {
            return tokens.userId(token).orElseThrow(() -> new IllegalArgumentException("未登录"));
        }
    }

    public static void runTests() {
        ShopApp app = new ShopApp();
        String token = app.registerAndLogin("alice", "pwd");
        Product p = app.createProduct(token, "book", 2000, 5);
        if (app.getProduct(p.id()).stock() != 5) {
            throw new AssertionError("读商品失败");
        }
        // 第二次读应走缓存：改 DB 偷偷变脏也不该立刻看见——这里只断言能读
        Order o = app.placeOrder(token, p.id(), 2);
        if (app.getProduct(p.id()).stock() != 3) {
            throw new AssertionError("下单后库存应为 3");
        }
        if (app.mq.handled != 1) {
            throw new AssertionError("消息应幂等只处理 1 次, handled=" + app.mq.handled);
        }
        if (!app.audit.toString().contains("NOTIFY:" + o.id()) || app.audit.toString().contains("DUP")) {
            throw new AssertionError("审计日志不对: " + app.audit);
        }
        boolean denied = false;
        try {
            app.createProduct("bad-token", "x", 1, 1);
        } catch (IllegalArgumentException ex) {
            denied = true;
        }
        if (!denied) {
            throw new AssertionError("无 token 应拒绝");
        }
        System.out.println("✅ MiniShopCapstone 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 MiniShopCapstone ************\n");
        ShopApp app = new ShopApp();
        String token = app.registerAndLogin("bob", "secret");
        Product p = app.createProduct(token, "pen", 300, 10);
        System.out.println("product=" + app.getProduct(p.id()));
        System.out.println("order=" + app.placeOrder(token, p.id(), 1));
        System.out.println("audit=" + app.audit);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
