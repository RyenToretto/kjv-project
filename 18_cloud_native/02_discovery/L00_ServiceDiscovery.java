import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [主题]: 服务发现与客户端负载均衡
 * [难度]: Medium
 * [前置]: 10_microservices
 *
 * 一、主题说明
 * 注册中心（Nacos/Eureka/Consul）：实例上报地址；调用方查询实例列表并负载均衡。
 * 本课实现 Registry + RoundRobin。
 *
 * 二、写法对比
 * 1. 直观版：写死 URL。
 * 2. 进阶版：发现 + 轮询。
 *
 * 三、真实用途
 * - 微服务互调；结合健康检查摘除实例。
 */
public class L00_ServiceDiscovery {

    record Instance(String id, String host, int port, boolean healthy) {}

    static class Registry {
        private final Map<String, List<Instance>> services = new ConcurrentHashMap<>();

        synchronized void register(String name, Instance inst) {
            services.computeIfAbsent(name, k -> new ArrayList<>()).add(inst);
        }

        synchronized void deregister(String name, String id) {
            List<Instance> list = services.get(name);
            if (list != null) {
                list.removeIf(i -> i.id().equals(id));
            }
        }

        synchronized List<Instance> healthy(String name) {
            List<Instance> list = services.getOrDefault(name, List.of());
            List<Instance> out = new ArrayList<>();
            for (Instance i : list) {
                if (i.healthy()) {
                    out.add(i);
                }
            }
            return out;
        }
    }

    static class RoundRobin {
        private final AtomicInteger idx = new AtomicInteger();

        Instance choose(List<Instance> list) {
            if (list.isEmpty()) {
                return null;
            }
            int i = Math.floorMod(idx.getAndIncrement(), list.size());
            return list.get(i);
        }
    }

    public static String urlOf(Instance i) {
        return "http://" + i.host() + ":" + i.port();
    }

    public static void runTests() {
        Registry reg = new Registry();
        reg.register("order", new Instance("1", "10.0.0.1", 8080, true));
        reg.register("order", new Instance("2", "10.0.0.2", 8080, true));
        reg.register("order", new Instance("3", "10.0.0.3", 8080, false));
        List<Instance> healthy = reg.healthy("order");
        if (healthy.size() != 2) {
            throw new AssertionError("健康实例应为 2");
        }
        RoundRobin lb = new RoundRobin();
        Instance a = lb.choose(healthy);
        Instance b = lb.choose(healthy);
        if (a.id().equals(b.id())) {
            throw new AssertionError("轮询应切换实例");
        }
        if (!urlOf(a).startsWith("http://10.0.0.")) {
            throw new AssertionError("url 格式");
        }
        reg.deregister("order", "1");
        if (reg.healthy("order").size() != 1) {
            throw new AssertionError("注销后剩 1");
        }
        System.out.println("✅ ServiceDiscovery 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ServiceDiscovery ************\n");
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
