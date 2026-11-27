import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: JDBC 编程模式 —— Connection / PreparedStatement / ResultSet 心智
 * [难度]: Medium
 * [前置]: 00_sql
 *
 * 一、主题说明
 * JDBC 是 JDK 访问关系库的标准 API。真实代码：
 *   Class.forName / DriverManager.getConnection → PreparedStatement → ResultSet
 * 本课用「迷你 JDBC」模拟相同调用顺序，强调：
 * - 永远用预编译参数，禁止字符串拼接 SQL（防注入）
 * - 资源必须关闭（生产用 try-with-resources）
 *
 * 二、写法对比
 * 1. 直观版（危险）：拼接 SQL。
 * 2. 进阶版：占位符 ? + setString。
 *
 * 三、真实用途
 * - 遗留系统、批量任务、框架底层；理解它才能读懂 MyBatis/JPA。
 */

/** 迷你库。 */
class MiniDb {
    final Map<String, List<Map<String, String>>> tables = new LinkedHashMap<>();

    MiniConnection open() {
        return new MiniConnection(this);
    }
}

class MiniConnection implements AutoCloseable {
    final MiniDb db;
    boolean closed;

    MiniConnection(MiniDb db) {
        this.db = db;
    }

    MiniPreparedStatement prepare(String sql) {
        ensureOpen();
        return new MiniPreparedStatement(this, sql);
    }

    void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("connection closed");
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}

class MiniPreparedStatement implements AutoCloseable {
    final MiniConnection conn;
    final String sql;
    final List<String> params = new ArrayList<>();

    MiniPreparedStatement(MiniConnection conn, String sql) {
        this.conn = conn;
        this.sql = sql;
    }

    void setString(int index1Based, String value) {
        while (params.size() < index1Based) {
            params.add(null);
        }
        params.set(index1Based - 1, value);
    }

    /** 仅支持: SELECT * FROM t WHERE col = ? */
    MiniResultSet executeQuery() {
        conn.ensureOpen();
        String[] parts = sql.split(" ", -1);
        // 极简解析
        if (!sql.toUpperCase().startsWith("SELECT * FROM ")) {
            throw new IllegalArgumentException("demo 仅支持 SELECT * FROM t WHERE col = ?");
        }
        String rest = sql.substring("SELECT * FROM ".length());
        String[] wp = rest.split("(?i) WHERE ", 2);
        String table = wp[0].trim();
        List<Map<String, String>> rows = conn.db.tables.getOrDefault(table, List.of());
        if (wp.length == 1) {
            return new MiniResultSet(rows);
        }
        String where = wp[1].trim(); // col = ?
        String col = where.split("=")[0].trim();
        String expect = params.get(0);
        List<Map<String, String>> filtered = new ArrayList<>();
        for (Map<String, String> r : rows) {
            if (expect != null && expect.equals(r.get(col))) {
                filtered.add(r);
            }
        }
        return new MiniResultSet(filtered);
    }

    /** 危险对照：把用户输入直接拼进 SQL（演示注入）。 */
    static String naiveUnsafeSql(String table, String status) {
        return "SELECT * FROM " + table + " WHERE status = '" + status + "'";
    }

    @Override
    public void close() {
        // no-op
    }
}

class MiniResultSet {
    final List<Map<String, String>> rows;
    int cursor = -1;

    MiniResultSet(List<Map<String, String>> rows) {
        this.rows = rows;
    }

    boolean next() {
        cursor++;
        return cursor < rows.size();
    }

    String getString(String col) {
        return rows.get(cursor).get(col);
    }
}

public class L00_JdbcPattern {

    public static MiniDb seed() {
        MiniDb db = new MiniDb();
        List<Map<String, String>> users = new ArrayList<>();
        users.add(Map.of("id", "1", "name", "alice", "status", "ACTIVE"));
        users.add(Map.of("id", "2", "name", "bob", "status", "ACTIVE"));
        db.tables.put("users", users);
        return db;
    }

    public static List<String> findActiveNames(MiniDb db) {
        List<String> names = new ArrayList<>();
        try (MiniConnection conn = db.open();
             MiniPreparedStatement ps = conn.prepare("SELECT * FROM users WHERE status = ?")) {
            ps.setString(1, "ACTIVE");
            MiniResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    /** 注入示例：status 被篡改成永远为真的条件时，拼接版会「查出更多」。 */
    public static boolean injectionBreaksNaiveFilter(String evilStatus) {
        String sql = MiniPreparedStatement.naiveUnsafeSql("users", evilStatus);
        // 若包含 OR '1'='1 一类，认为拼接已被注入改写
        return sql.contains("OR") || sql.contains("--");
    }

    public static void runTests() {
        MiniDb db = seed();
        List<String> names = findActiveNames(db);
        if (!names.equals(List.of("alice", "bob"))) {
            throw new AssertionError("预编译查询失败: " + names);
        }
        if (!injectionBreaksNaiveFilter("x' OR '1'='1")) {
            throw new AssertionError("应能演示拼接被注入改写");
        }
        // 关闭后不可用
        MiniConnection c = db.open();
        c.close();
        boolean closed = false;
        try {
            c.prepare("SELECT * FROM users WHERE status = ?");
        } catch (IllegalStateException ex) {
            closed = true;
        }
        if (!closed) {
            throw new AssertionError("关闭后应不可 prepare");
        }
        System.out.println("✅ JdbcPattern 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 JdbcPattern ************\n");
        System.out.println(findActiveNames(seed()));
        System.out.println("unsafe sql demo: "
                + MiniPreparedStatement.naiveUnsafeSql("users", "x' OR '1'='1"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
