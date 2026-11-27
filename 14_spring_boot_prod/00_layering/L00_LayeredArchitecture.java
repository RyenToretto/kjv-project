import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * [主题]: 生产分层 —— Controller / Service / Repository / DTO
 * [难度]: Medium
 * [前置]: 06_spring_boot、07_persistence、13_project_capstone
 *
 * 一、主题说明
 * Spring Boot 生产项目通常四层：
 * - Controller：HTTP 进出、参数绑定，不写业务
 * - Service：业务规则、事务边界
 * - Repository：持久化
 * - DTO / Entity 分离：对外契约 ≠ 表结构
 * 本课用纯 JDK 复刻分层调用链，对应真实 @RestController / @Service / JpaRepository。
 *
 * 二、写法对比
 * 1. 直观版：Controller 直接操作 Map「数据库」。
 * 2. 进阶版：严格分层 + DTO 映射。
 *
 * 三、真实用途
 * - 中小型 API 标配结构；代码评审先看有没有「Controller 里写 SQL/业务」。
 */
public class L00_LayeredArchitecture {

    record ProductEntity(long id, String name, int priceCents) {}

    record ProductResponse(long id, String name, String priceYuan) {}

    record CreateProductRequest(String name, int priceCents) {}

    interface ProductRepository {
        ProductEntity save(ProductEntity e);

        Optional<ProductEntity> findById(long id);
    }

    static class InMemoryProductRepository implements ProductRepository {
        private final AtomicLong seq = new AtomicLong(1);
        private final Map<Long, ProductEntity> db = new HashMap<>();

        @Override
        public synchronized ProductEntity save(ProductEntity e) {
            long id = e.id() == 0 ? seq.getAndIncrement() : e.id();
            ProductEntity stored = new ProductEntity(id, e.name(), e.priceCents());
            db.put(id, stored);
            return stored;
        }

        @Override
        public synchronized Optional<ProductEntity> findById(long id) {
            return Optional.ofNullable(db.get(id));
        }
    }

    static class ProductService {
        private final ProductRepository repo;

        ProductService(ProductRepository repo) {
            this.repo = repo;
        }

        ProductEntity create(CreateProductRequest req) {
            if (req.name() == null || req.name().isBlank()) {
                throw new IllegalArgumentException("name 必填");
            }
            if (req.priceCents() <= 0) {
                throw new IllegalArgumentException("price 必须 > 0");
            }
            return repo.save(new ProductEntity(0, req.name().trim(), req.priceCents()));
        }

        ProductEntity get(long id) {
            return repo.findById(id).orElseThrow(() -> new NotFoundException("商品不存在: " + id));
        }
    }

    static class NotFoundException extends RuntimeException {
        NotFoundException(String msg) {
            super(msg);
        }
    }

    static class ProductController {
        private final ProductService service;

        ProductController(ProductService service) {
            this.service = service;
        }

        record HttpResult(int status, Object body) {}

        HttpResult create(CreateProductRequest req) {
            ProductEntity e = service.create(req);
            return new HttpResult(201, toDto(e));
        }

        HttpResult get(long id) {
            try {
                return new HttpResult(200, toDto(service.get(id)));
            } catch (NotFoundException ex) {
                return new HttpResult(404, Map.of("message", ex.getMessage()));
            }
        }

        static ProductResponse toDto(ProductEntity e) {
            String yuan = String.format("%.2f", e.priceCents() / 100.0);
            return new ProductResponse(e.id(), e.name(), yuan);
        }
    }

    /** 直观版：无分层。 */
    public static String createNaive(Map<Long, String> db, AtomicLong seq, String name) {
        long id = seq.getAndIncrement();
        db.put(id, name);
        return id + ":" + name;
    }

    public static ProductController wire() {
        return new ProductController(new ProductService(new InMemoryProductRepository()));
    }

    public static void runTests() {
        ProductController api = wire();
        var created = api.create(new CreateProductRequest("book", 1999));
        if (created.status() != 201) {
            throw new AssertionError("创建应为 201");
        }
        ProductResponse body = (ProductResponse) created.body();
        if (!"book".equals(body.name()) || !"19.99".equals(body.priceYuan())) {
            throw new AssertionError("DTO 映射错误: " + body);
        }
        var got = api.get(body.id());
        if (got.status() != 200) {
            throw new AssertionError("查询应为 200");
        }
        if (api.get(9999).status() != 404) {
            throw new AssertionError("缺失应为 404");
        }
        boolean bad = false;
        try {
            api.create(new CreateProductRequest("  ", 1));
        } catch (IllegalArgumentException ex) {
            bad = true;
        }
        if (!bad) {
            throw new AssertionError("非法 name 应失败");
        }
        Map<Long, String> naive = new HashMap<>();
        if (!createNaive(naive, new AtomicLong(1), "x").startsWith("1:")) {
            throw new AssertionError("naive 失败");
        }
        System.out.println("✅ LayeredArchitecture 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 LayeredArchitecture ************\n");
        ProductController api = wire();
        System.out.println(api.create(new CreateProductRequest("pen", 300)));
        System.out.println(api.get(1));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
