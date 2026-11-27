/**
 * [主题]: 模板方法 —— 固定流程，子类填步骤
 * [难度]: Medium
 * [前置]: 05 继承
 *
 * 一、主题说明
 * 模板方法：父类定义算法骨架，子类实现差异步骤。
 * Spring：JdbcTemplate、RestTemplate、各类 *Template 命名即此思想（不完全是继承实现）。
 *
 * 二、写法对比
 * 1. 直观版：两处复制「打开-处理-关闭」流程。
 * 2. 进阶版：抽象 process() 模板。
 *
 * 三、真实用途
 * - 导入文件、支付回调验签→处理→应答。
 */
public class L00_TemplateMethod {

    abstract static class DataImporter {
        final StringBuilder log = new StringBuilder();

        /** 模板方法：流程固定。 */
        public final String process(String raw) {
            log.append("open;");
            String normalized = normalize(raw);
            log.append("handle;");
            String result = handle(normalized);
            log.append("close;");
            return result;
        }

        protected String normalize(String raw) {
            return raw.trim();
        }

        protected abstract String handle(String data);
    }

    static class UpperImporter extends DataImporter {
        @Override
        protected String handle(String data) {
            return data.toUpperCase();
        }
    }

    static class CsvCountImporter extends DataImporter {
        @Override
        protected String handle(String data) {
            if (data.isEmpty()) {
                return "0";
            }
            return String.valueOf(data.split(",").length);
        }
    }

    public static void runTests() {
        UpperImporter u = new UpperImporter();
        if (!"HI".equals(u.process("  hi "))) {
            throw new AssertionError("Upper 失败");
        }
        if (!"open;handle;close;".equals(u.log.toString())) {
            throw new AssertionError("模板步骤日志不对: " + u.log);
        }
        CsvCountImporter c = new CsvCountImporter();
        if (!"3".equals(c.process("a,b,c"))) {
            throw new AssertionError("CSV count 失败");
        }
        System.out.println("✅ TemplateMethod 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 TemplateMethod ************\n");
        System.out.println(new UpperImporter().process("java"));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
