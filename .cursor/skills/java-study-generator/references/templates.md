# `kjv-project` 学习文件标准模板

## 1. 单课 `.java` 模板（入门无 package，可被 verify 直接 javac）

```java
/**
 * [主题]: 一句话标题
 * [难度]: Easy / Medium / Hard
 * [前置]: 无 / 00_java_basics/...
 *
 * 一、主题说明
 * ...
 *
 * 二、写法对比
 * 1. 直观版: ...
 * 2. 进阶版: ...
 *
 * 三、真实用途
 * - 业务后端 / 并发 / 数据访问: ...
 */
public class L00_HelloAndTypes {

    /** 直观版示例逻辑 */
    public static int addNaive(int left, int right) {
        // 中文注释
        return left + right;
    }

    /** 进阶版（有对比价值时再写） */
    public static int addBetter(int left, int right) {
        return Math.addExact(left, right);
    }

    public static void runTests() {
        if (addNaive(1, 2) != 3) {
            throw new AssertionError("1+2 应为 3");
        }
        if (addBetter(2, 3) != 5) {
            throw new AssertionError("2+3 应为 5");
        }
        System.out.println("✅ HelloAndTypes 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ HelloAndTypes ************\n");
        System.out.println("演示: " + addNaive(10, 20));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
```

## 2. 阶段 `README.md` 模板

```markdown
# `NN_slug` | 中文名

- **目标**: ...
- **前置**: ...
- **环境**: JDK 25+，`./scripts/verify_modules.sh NN_slug`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | ... | ... | ✅ / 📝 |

**本目录总课数：N（已完成 x，待生成 y）**
```

## 3. 命名

- 目录：`00_syntax/`、`01_oop/`…
- 文件/类：`L00_HelloAndTypes`（`L` + 两位序号 + `_` + PascalCase）
