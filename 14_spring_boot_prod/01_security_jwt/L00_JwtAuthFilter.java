import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * [主题]: JWT 鉴权过滤器心智 —— 登录发牌、请求验牌、Filter 链
 * [难度]: Medium～Hard
 * [前置]: 00_layering、12 责任链
 *
 * 一、主题说明
 * 生产常用无状态 JWT：登录成功签发 token；后续请求 Authorization: Bearer &lt;token&gt;。
 * Spring Security 过滤器链在 Controller 前拦截。本课实现「迷你 JWT」（Base64 载荷 + HMAC 简化签名），
 * 教学用，勿当生产加密库；真实项目用 jjwt / nimbus / Spring Authorization Server。
 *
 * 二、写法对比
 * 1. 直观版：服务端 Session Map 存登录态。
 * 2. 进阶版：无状态 JWT + Filter 注入当前用户。
 *
 * 三、真实用途
 * - SPA / 移动端 API；微服务间也可传 JWT（或 mTLS）。
 */
public class L00_JwtAuthFilter {

    record UserPrincipal(long userId, String username) {}

    /** 简化 JWT：header.payload.signature（signature = Base64(secret+payload) 演示用）。 */
    static class MiniJwt {
        private final String secret;

        MiniJwt(String secret) {
            this.secret = secret;
        }

        String issue(long userId, String username, long expEpochSec) {
            String payload = userId + "|" + username + "|" + expEpochSec;
            String p = b64(payload);
            String s = b64(secret + p);
            return "hdr." + p + "." + s;
        }

        Optional<UserPrincipal> parse(String token, long nowEpochSec) {
            if (token == null || !token.startsWith("hdr.")) {
                return Optional.empty();
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String expect = b64(secret + parts[1]);
            if (!expect.equals(parts[2])) {
                return Optional.empty(); // 签名不对
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String[] f = payload.split("\\|");
            if (f.length != 3) {
                return Optional.empty();
            }
            long exp = Long.parseLong(f[2]);
            if (nowEpochSec > exp) {
                return Optional.empty();
            }
            return Optional.of(new UserPrincipal(Long.parseLong(f[0]), f[1]));
        }

        private static String b64(String raw) {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }
    }

    static class SessionStore {
        final Map<String, UserPrincipal> sessions = new HashMap<>();

        String login(UserPrincipal u) {
            String sid = "S-" + u.userId();
            sessions.put(sid, u);
            return sid;
        }

        Optional<UserPrincipal> get(String sid) {
            return Optional.ofNullable(sessions.get(sid));
        }
    }

    record Request(String path, String authorization) {}

    record Response(int status, String body) {}

    static class AuthFilter {
        private final MiniJwt jwt;
        private final long now;

        AuthFilter(MiniJwt jwt, long now) {
            this.jwt = jwt;
            this.now = now;
        }

        Response filter(Request req, java.util.function.Function<UserPrincipal, Response> next) {
            if (req.path().startsWith("/public/")) {
                return next.apply(null);
            }
            String auth = req.authorization();
            if (auth == null || !auth.startsWith("Bearer ")) {
                return new Response(401, "missing token");
            }
            Optional<UserPrincipal> user = jwt.parse(auth.substring("Bearer ".length()), now);
            if (user.isEmpty()) {
                return new Response(401, "invalid token");
            }
            return next.apply(user.get());
        }
    }

    public static void runTests() {
        MiniJwt jwt = new MiniJwt("dev-secret");
        long now = 1_700_000_000L;
        String token = jwt.issue(1, "alice", now + 3600);
        if (jwt.parse(token, now).isEmpty() || !"alice".equals(jwt.parse(token, now).get().username())) {
            throw new AssertionError("合法 token 应解析成功");
        }
        if (jwt.parse(token, now + 99999).isPresent()) {
            throw new AssertionError("过期应失败");
        }
        if (jwt.parse(token + "x", now).isPresent()) {
            throw new AssertionError("篡改应失败");
        }

        AuthFilter filter = new AuthFilter(jwt, now);
        Response denied = filter.filter(new Request("/api/me", null), u -> new Response(200, "ok"));
        if (denied.status() != 401) {
            throw new AssertionError("无 token 应 401");
        }
        Response ok = filter.filter(new Request("/api/me", "Bearer " + token),
                u -> new Response(200, u.username()));
        if (ok.status() != 200 || !"alice".equals(ok.body())) {
            throw new AssertionError("带 token 应进入业务: " + ok);
        }
        Response pub = filter.filter(new Request("/public/health", null), u -> new Response(200, "up"));
        if (pub.status() != 200) {
            throw new AssertionError("公开路径应放行");
        }

        SessionStore sessions = new SessionStore();
        String sid = sessions.login(new UserPrincipal(2, "bob"));
        if (!"bob".equals(sessions.get(sid).orElseThrow().username())) {
            throw new AssertionError("Session 版对照失败");
        }
        System.out.println("✅ JwtAuthFilter 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 JwtAuthFilter ************\n");
        MiniJwt jwt = new MiniJwt("dev-secret");
        long now = 1_700_000_000L;
        String token = jwt.issue(1, "alice", now + 60);
        System.out.println("token=" + token);
        System.out.println(jwt.parse(token, now));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
