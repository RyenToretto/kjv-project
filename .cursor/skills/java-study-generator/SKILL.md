---
name: java-study-generator
description: >-
  Generate structured Java learning modules for kjv-project (00_java_basics,
  01_java8_plus, 06_spring_boot, ...). Produces 00_ dirs, L00_*.java lessons with
  Chinese comments, runTests()+bootstrap()+main, and scripts/verify_modules.sh.
  Use when initializing or expanding any NN_xxx stage under kjv-project, or
  auditing Java study lessons.
---

# Java Study Generator (`kjv-project`)

为 `kjv-project` 生成可直接运行的 Java 学习课，对齐仓库规范：中文注释、直观版 + 进阶版、`runTests()` + `bootstrap()` + `main`、`./scripts/verify_modules.sh` 可过。

## 何时启用

- 「按 kjv / java-study-generator 初始化 `01_java8_plus` / `06_spring_boot` …」
- 「全自动生成下一阶段 / 把路线图全部做完」
- 在 `kjv-project` 内新增编号学习目录或审计课是否合格

## Workflow

1. **读路线图，定目录名**
   - 根 [README.md](../../../README.md) 与 [references/roadmap.md](references/roadmap.md)。
   - 阶段目录：`<repo-root>/NN_<slug>/`。
   - 起点：**Java 小白**；后端主线目标是 **Spring Boot 就业级**。

2. **读模板再写代码**
   - 必读 [references/templates.md](references/templates.md)。
   - 必读 [.cursor/rules/kjv-learning-standards.mdc](../../rules/kjv-learning-standards.mdc)。

3. **源码落盘约定（重要）**
   - 课文件放在：`NN_slug/<topic>/L00_TopicName.java`，同时为了 Maven/javac 简便，**verify 脚本按「可独立 javac 的单文件课」或「src/main/java 包路径」两种都能跑**；优先采用：

```text
00_java_basics/00_syntax/L00_HelloAndTypes.java
```

   - 类名：`L00_HelloAndTypes`（必须与文件名一致）。
   - 无 package 的入门单文件课：允许（小白最先接触）；进入集合/多文件后改用 `package kjv.s00_java_basics.syntax;` 并把文件放到

```text
00_java_basics/src/main/java/kjv/s00_java_basics/syntax/L00_HelloAndTypes.java
```

   - 同一阶段内风格保持一致；升级结构时更新该阶段 README。

4. **生成内容约束**
   - 顶部主题块、中文逐行注释、有对比价值时给直观版 + 进阶版。
   - `runTests()` 用 `if (!cond) throw new AssertionError("...")`；打印 `✅ ...通过`。
   - `bootstrap()` 做演示，最后调用 `runTests()`；`public static void main` 调 `bootstrap()`。
   - 离线可测；新依赖写入 `pom.xml`。
   - 更新阶段 README 与根 README「已生成」表。

5. **分批提交（用户要求全自动时）**
   - 每完成一个 `NN_xxx`：verify 通过 → 单独 commit；全部完成再 push（用户要求时）。
   - 不要 force push。

6. **验证**

```bash
./scripts/verify_modules.sh 00_java_basics
./scripts/verify_modules.sh 00_java_basics/00_syntax
```

## 用户一句话触发示例

```text
按 java-study-generator / kjv 学习规范初始化 00_java_basics
```

```text
全自动把 README 阶段路线里还没生成的目录都做完，分批次提交并 push
```

## Additional Resources

- [references/templates.md](references/templates.md)
- [references/roadmap.md](references/roadmap.md)
- 仓库 `scripts/verify_modules.sh`
