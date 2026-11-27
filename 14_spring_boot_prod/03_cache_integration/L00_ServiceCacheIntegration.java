import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: Service 层接入缓存 —— @Cacheable 心智与写后失效
 * [难度]: Medium
 * [前置]: 08_redis、00_layering
 *
 * 一、主题说明
 * Spring Cache：@Cacheable 读穿透回填；@CacheEvict 写后删。
 * 本课在 Service 外包一层缓存装饰器，对应 RedisCacheManager 行为。
 *
 * 二、写法对比
 * 1. 直观版：每次 Service 打仓储。
 * 2. 进阶版：读缓存 / 写失效。
 *
 * 三、真实用途
 * - 商品详情、配置项；注意方法内部自调用同样不走代理。
 */
public class L00_ServiceCacheIntegration {

    record Product(long id, String name) {}

    interface ProductService {
        Product get(long id);

        Product rename(long id, String name);
    }

    static class ProductServiceImpl implements ProductService {
        final Map<Long, Product> db = new HashMap<>();
        final AtomicInteger dbHits = new AtomicInteger();

        ProductServiceImpl() {
            db.put(1L, new Product(1, "book"));
        }

        @Override
        public Product get(long id) {
            dbHits.incrementAndGet();
            Product p = db.get(id);
            if (p == null) {
                throw new IllegalArgumentException("not found");
            }
            return p;
        }

        @Override
        public Product rename(long id, String name) {
            Product p = get(id);
            Product n = new Product(id, name);
            db.put(id, n);
            return n;
        }
    }

    static class CachedProductService implements ProductService {
        private final ProductService delegate;
        private final Map<Long, Product> cache = new HashMap<>();

        CachedProductService(ProductService delegate) {
            this.delegate = delegate;
        }

        @Override
        public Product get(long id) {
            Product cached = cache.get(id);
            if (cached != null) {
                return cached;
            }
            Product p = delegate.get(id);
            cache.put(id, p);
            return p;
        }

        @Override
        public Product rename(long id, String name) {
            Product p = delegate.rename(id, name);
            cache.remove(id); // @CacheEvict
            return p;
        }
    }

    public static void runTests() {
        ProductServiceImpl raw = new ProductServiceImpl();
        ProductService cached = new CachedProductService(raw);
        if (!"book".equals(cached.get(1).name())) {
            throw new AssertionError("首次读取失败");
        }
        cached.get(1);
        if (raw.dbHits.get() != 1) {
            throw new AssertionError("第二次应命中缓存, dbHits=" + raw.dbHits.get());
        }
        cached.rename(1, "book2");
        if (!"book2".equals(cached.get(1).name())) {
            throw new AssertionError("写后应读到新值");
        }
        // rename 内 get + 之后 get = 额外 DB；允许 >= 2 次相对首次之后
        if (raw.dbHits.get() < 2) {
            throw new AssertionError("失效后应再次访问 DB");
        }
        System.out.println("✅ ServiceCacheIntegration 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ServiceCacheIntegration ************\n");
        ProductServiceImpl raw = new ProductServiceImpl();
        ProductService cached = new CachedProductService(raw);
        System.out.println(cached.get(1));
        System.out.println(cached.get(1) + " dbHits=" + raw.dbHits.get());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
