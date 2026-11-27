import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * [主题]: 分布式追踪心智 —— TraceId / SpanId 传播
 * [难度]: Medium～Hard
 * [前置]: 10、14 ProblemDetails traceId
 *
 * 一、主题说明
 * OpenTelemetry：一次请求一个 Trace，每跳一个 Span；经 HTTP 头传播（traceparent）。
 * 本课实现 ThreadLocal 上下文 + 跨「服务」调用拷贝。
 *
 * 二、写法对比
 * 1. 直观版：各服务自己打日志，无法关联。
 * 2. 进阶版：注入/提取 trace 头，日志带同一 traceId。
 *
 * 三、真实用途
 * - 排障跨服务延迟；Jaeger/Tempo/SkyWalking。
 */
public class L00_TraceContextPropagation {

    record SpanContext(String traceId, String spanId, String parentSpanId) {}

    static class Tracer {
        private static final ThreadLocal<SpanContext> CTX = new ThreadLocal<>();

        static SpanContext current() {
            return CTX.get();
        }

        static SpanContext startRoot() {
            SpanContext ctx = new SpanContext(id(), id(), null);
            CTX.set(ctx);
            return ctx;
        }

        static SpanContext startChild() {
            SpanContext parent = CTX.get();
            if (parent == null) {
                return startRoot();
            }
            SpanContext child = new SpanContext(parent.traceId(), id(), parent.spanId());
            CTX.set(child);
            return child;
        }

        static void clear() {
            CTX.remove();
        }

        static void inject(Map<String, String> headers) {
            SpanContext c = CTX.get();
            if (c != null) {
                headers.put("traceparent", c.traceId() + "-" + c.spanId());
            }
        }

        static void extract(Map<String, String> headers) {
            String tp = headers.get("traceparent");
            if (tp == null) {
                startRoot();
                return;
            }
            String[] p = tp.split("-");
            CTX.set(new SpanContext(p[0], id(), p[1]));
        }

        private static String id() {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    public static String serviceA() {
        Tracer.startRoot();
        String tid = Tracer.current().traceId();
        Map<String, String> headers = new HashMap<>();
        Tracer.inject(headers);
        // 下游服务有独立 ThreadLocal，会 clear；上游先保存 tid
        String remote = serviceB(headers);
        Tracer.clear();
        return tid + "|" + remote;
    }

    public static String serviceB(Map<String, String> headers) {
        Tracer.extract(headers);
        String tid = Tracer.current().traceId();
        String parent = Tracer.current().parentSpanId();
        Tracer.clear();
        return tid + ":" + parent;
    }

    public static void runTests() {
        String result = serviceA();
        String[] parts = result.split("\\|");
        String tidA = parts[0];
        String tidB = parts[1].split(":")[0];
        if (!tidA.equals(tidB)) {
            throw new AssertionError("跨服务 traceId 应相同: " + result);
        }
        if (Tracer.current() != null) {
            throw new AssertionError("应清理 ThreadLocal");
        }
        Map<String, String> h = new HashMap<>();
        Tracer.startRoot();
        String root = Tracer.current().traceId();
        Tracer.inject(h);
        Tracer.clear();
        Tracer.extract(h);
        if (!root.equals(Tracer.current().traceId())) {
            throw new AssertionError("extract 应恢复 traceId");
        }
        Tracer.clear();
        System.out.println("✅ TraceContextPropagation 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 TraceContextPropagation ************\n");
        System.out.println(serviceA());
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
