---
name: java-study-generator
description: >-
  Generate structured Java learning modules for kjv-project (00_java_basics …
  19_architecture). Produces NN_ dirs, L00_*.java lessons with Chinese comments,
  runTests()+bootstrap()+main, and scripts/verify_modules.sh. Use when initializing
  or expanding any NN_xxx stage, running full-auto roadmap generation, or auditing lessons.
---

# Java Study Generator (`kjv-project`)

为 `kjv-project` 生成可直接运行的 Java 学习课，对齐仓库规范：中文注释、直观版 + 进阶版、`runTests()` + `bootstrap()` + `main`、`./scripts/verify_modules.sh` 可过。

## 何时启用

- 「按 kjv / java-study-generator 初始化 `01_java8_plus` / `06_spring_boot` / `14_spring_boot_prod` …」
- 「全自动生成下一阶段 / 把路线图全部做完 / 推进 14→17→16→15→18→19」
- 在 `kjv-project` 内新增编号学习目录或审计课是否合格

## Workflow

1. **读路线图，定目录名**
   - 根 [README.md](../../../README.md) 与 [references/roadmap.md](references/roadmap.md)。
   - 阶段目录：`<repo-root>/NN_<slug>/`。
   - 起点：**Java 小白**；先就业主线 `00_`～`13_`，再精通线 `14→17→16→15→18→19`。
   - 工具链：**JDK 25+**（`maven.compiler.release=25`）；不要按 17 降级教学。

2. **读模板再写代码**
   - 必读 [references/templates.md](references/templates.md)。
   - 必读 [.cursor/rules/kjv-learning-standards.mdc](../../rules/kjv-learning-standards.mdc)。

3. **源码落盘约定**
   - 优先：`NN_slug/<topic>/L00_TopicName.java`（无 package，verify 可直接 javac）。
   - 类名与文件名一致；多文件阶段保持同阶段风格一致。
   - 生产框架阶段：用**纯 JDK 迷你实现**讲清心智（DI/REST/事务/缓存/MQ/网关等），在 README 写明与 Spring Boot / 真实中间件的对照；禁止默认已会注解驱动。

4. **生成内容约束**
   - 顶部主题块、中文注释、有价值时直观版 + 进阶版。
   - `runTests()` 失败抛 `AssertionError`；打印 `✅ ...通过`。
   - `bootstrap()` 演示后调用 `runTests()`；`main` → `bootstrap()`。
   - 离线可测；避免脆弱的时序断言；并发课注意唤醒丢失等正确性。
   - 更新阶段 README 与根 README「已生成」表。

5. **分批提交（用户要求全自动时）**
   - 每完成一个 `NN_xxx`：verify 通过 → **单独 commit**；全部完成再 push。
   - 不要 force push；不要无关重构。

6. **验证**

```bash
./scripts/verify_modules.sh 00_java_basics
./scripts/verify_modules.sh 14_spring_boot_prod
```

## 用户一句话触发示例

```text
按 java-study-generator / kjv 学习规范初始化 00_java_basics
```

```text
全自动把 README 阶段路线里还没生成的目录都做完，分批次提交并 push
```

```text
请帮我全自动推进14 → 17 → 16 → 15 → 18 → 19，保证全面，细节完美
```

## Additional Resources

- [references/templates.md](references/templates.md)
- [references/roadmap.md](references/roadmap.md)
- 仓库 `scripts/verify_modules.sh`
