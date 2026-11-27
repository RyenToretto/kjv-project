import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [主题]: N+1 查询问题 —— 为何变慢、如何一次取回
 * [难度]: Medium
 * [前置]: 01_repository
 *
 * 一、主题说明
 * N+1：先查列表（1 次），再对每个元素查关联（N 次），共 N+1 次访问。
 * ORM 懒加载稍不注意就中招。解法：join fetch / 批量 IN 查询 / DTO 投影。
 *
 * 二、写法对比
 * 1. 直观版：循环里再查作者（N+1）。
 * 2. 进阶版：一次查出作者 Map，内存组装。
 *
 * 三、真实用途
 * - 列表页性能事故高频原因；结合日志打印 SQL 条数排查。
 */
public class L00_NplusOneProblem {

    record Author(long id, String name) {}

    record Book(long id, String title, long authorId) {}

    record BookView(String title, String authorName) {}

    static class Db {
        final List<Book> books = new ArrayList<>();
        final Map<Long, Author> authors = new HashMap<>();
        int queryCount;

        List<Book> findAllBooks() {
            queryCount++;
            return List.copyOf(books);
        }

        Author findAuthor(long id) {
            queryCount++;
            return authors.get(id);
        }

        Map<Long, Author> findAuthorsByIds(List<Long> ids) {
            queryCount++;
            Map<Long, Author> m = new HashMap<>();
            for (Long id : ids) {
                if (authors.containsKey(id)) {
                    m.put(id, authors.get(id));
                }
            }
            return m;
        }
    }

    public static List<BookView> loadNaive(Db db) {
        List<BookView> out = new ArrayList<>();
        for (Book b : db.findAllBooks()) {
            Author a = db.findAuthor(b.authorId());
            out.add(new BookView(b.title(), a.name()));
        }
        return out;
    }

    public static List<BookView> loadBetter(Db db) {
        List<Book> books = db.findAllBooks();
        List<Long> ids = books.stream().map(Book::authorId).distinct().toList();
        Map<Long, Author> authors = db.findAuthorsByIds(ids);
        List<BookView> out = new ArrayList<>();
        for (Book b : books) {
            out.add(new BookView(b.title(), authors.get(b.authorId()).name()));
        }
        return out;
    }

    public static Db seed() {
        Db db = new Db();
        db.authors.put(1L, new Author(1, "Tom"));
        db.authors.put(2L, new Author(2, "Ada"));
        db.books.add(new Book(1, "A", 1));
        db.books.add(new Book(2, "B", 1));
        db.books.add(new Book(3, "C", 2));
        return db;
    }

    public static void runTests() {
        Db n1 = seed();
        List<BookView> a = loadNaive(n1);
        if (a.size() != 3 || n1.queryCount != 1 + 3) {
            throw new AssertionError("N+1 应为 4 次查询, 实际=" + n1.queryCount);
        }
        Db batch = seed();
        List<BookView> b = loadBetter(batch);
        if (b.size() != 3 || batch.queryCount != 2) {
            throw new AssertionError("批量应为 2 次查询, 实际=" + batch.queryCount);
        }
        if (!a.get(0).authorName().equals(b.get(0).authorName())) {
            throw new AssertionError("结果应一致");
        }
        System.out.println("✅ NplusOneProblem 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 NplusOneProblem ************\n");
        Db db = seed();
        System.out.println(loadBetter(db) + " queries=" + db.queryCount);
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
