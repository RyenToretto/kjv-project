import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * [主题]: 依赖注入（DI）—— 为什么不自己 new，以及简易容器
 * [难度]: Medium
 * [前置]: 00_overview、00_java_basics 接口
 *
 * 一、主题说明
 * 硬编码 new：换实现就要改业务类，难测试。
 * DI：对象需要的依赖由「容器」从外面注入（构造器注入最推荐）。
 * Spring IoC 容器管理 Bean 生命周期；@Component / @Service 等是注册方式。
 *
 * 二、写法对比
 * 1. 直观版：Service 内部 new Repository。
 * 2. 进阶版：构造器注入；迷你 ApplicationContext。
 *
 * 三、真实用途
 * - 整个 Spring 生态的根基；单元测试可换 Fake 实现。
 */

interface UserRepo {
    String findName(String id);
}

class MapUserRepo implements UserRepo {
    private final Map<String, String> data = Map.of("1", "alice");

    @Override
    public String findName(String id) {
        return data.get(id);
    }
}

class UserServiceNaive {
    private final UserRepo repo = new MapUserRepo(); // 写死

    String hello(String id) {
        return "hi-" + repo.findName(id);
    }
}

class UserServiceBetter {
    private final UserRepo repo;

    UserServiceBetter(UserRepo repo) {
        this.repo = repo;
    }

    String hello(String id) {
        return "hi-" + repo.findName(id);
    }
}

/** 迷你容器：按类型注册与获取。 */
class MiniContext {
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();

    <T> void register(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    @SuppressWarnings("unchecked")
    <T> T get(Class<T> type) {
        // 不用 computeIfAbsent：工厂里可能再次 get 其它 Bean，会并发修改同一 HashMap
        Object existing = singletons.get(type);
        if (existing != null) {
            return (T) existing;
        }
        Supplier<?> factory = factories.get(type);
        if (factory == null) {
            throw new IllegalStateException("未注册: " + type.getSimpleName());
        }
        T created = (T) factory.get();
        singletons.put(type, created);
        return created;
    }
}

public class L00_DependencyInjection {

    public static UserServiceBetter wire() {
        MiniContext ctx = new MiniContext();
        ctx.register(UserRepo.class, MapUserRepo::new);
        ctx.register(UserServiceBetter.class, () -> new UserServiceBetter(ctx.get(UserRepo.class)));
        return ctx.get(UserServiceBetter.class);
    }

    public static void runTests() {
        if (!"hi-alice".equals(new UserServiceNaive().hello("1"))) {
            throw new AssertionError("naive 失败");
        }
        UserServiceBetter svc = wire();
        if (!"hi-alice".equals(svc.hello("1"))) {
            throw new AssertionError("DI 失败");
        }
        // 可替换实现
        UserServiceBetter fake = new UserServiceBetter(id -> "bob");
        if (!"hi-bob".equals(fake.hello("x"))) {
            throw new AssertionError("替换实现失败");
        }
        System.out.println("✅ DependencyInjection 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 DependencyInjection ************\n");
        System.out.println(wire().hello("1"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
