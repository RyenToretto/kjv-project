import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * [主题]: Repository 仓储模式 —— 隔离业务与存储
 * [难度]: Medium
 * [前置]: 00_orm_idea、06 DI
 *
 * 一、主题说明
 * Repository：用集合口吻操作聚合根（save/findById/delete），底层可以是内存、JDBC、JPA。
 * Spring Data：接口方法名派生查询（findByEmail）；本课手写接口 + 内存实现。
 *
 * 二、写法对比
 * 1. 直观版：Service 直接操作 List。
 * 2. 进阶版：ProductRepository 接口 + InMemory 实现。
 *
 * 三、真实用途
 * - 分层清晰；测试可换内存仓库；对接 JpaRepository 时概念不变。
 */
public class L00_RepositoryPattern {

    record Product(Long id, String name, int priceCents) {
        Product withId(Long newId) {
            return new Product(newId, name, priceCents);
        }
    }

    interface ProductRepository {
        Product save(Product p);

        Optional<Product> findById(Long id);

        List<Product> findAll();

        void deleteById(Long id);
    }

    static class InMemoryProductRepository implements ProductRepository {
        private final AtomicLong seq = new AtomicLong(1);
        private final List<Product> store = new ArrayList<>();

        @Override
        public synchronized Product save(Product p) {
            if (p.id() == null) {
                Product created = p.withId(seq.getAndIncrement());
                store.add(created);
                return created;
            }
            deleteById(p.id());
            store.add(p);
            return p;
        }

        @Override
        public synchronized Optional<Product> findById(Long id) {
            return store.stream().filter(x -> x.id().equals(id)).findFirst();
        }

        @Override
        public synchronized List<Product> findAll() {
            return List.copyOf(store);
        }

        @Override
        public synchronized void deleteById(Long id) {
            store.removeIf(x -> x.id().equals(id));
        }
    }

    static class ProductService {
        private final ProductRepository repo;

        ProductService(ProductRepository repo) {
            this.repo = repo;
        }

        Product create(String name, int price) {
            return repo.save(new Product(null, name, price));
        }

        Optional<Product> get(Long id) {
            return repo.findById(id);
        }
    }

    public static void runTests() {
        ProductRepository repo = new InMemoryProductRepository();
        ProductService svc = new ProductService(repo);
        Product p = svc.create("book", 2000);
        if (p.id() == null) {
            throw new AssertionError("保存后应有 id");
        }
        if (svc.get(p.id()).isEmpty() || !"book".equals(svc.get(p.id()).get().name())) {
            throw new AssertionError("查找失败");
        }
        repo.deleteById(p.id());
        if (svc.get(p.id()).isPresent()) {
            throw new AssertionError("删除后应 empty");
        }
        if (repo.findAll().size() != 0) {
            throw new AssertionError("应空列表");
        }
        System.out.println("✅ RepositoryPattern 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 RepositoryPattern ************\n");
        ProductService svc = new ProductService(new InMemoryProductRepository());
        System.out.println(svc.create("pen", 300));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
