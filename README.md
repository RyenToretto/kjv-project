# kjv-project

Java **从零到精通**的个人学习仓库。前提：你是 **Java 小白**（可以有其他语言基础，但 Java 从语法讲起）。目录按 `00_`、`01_`… 学习阶段递增。

## 目录（规划 / 已生成）

| 路径 | 阶段 | 内容 | 状态 |
| --- | --- | --- | --- |
| [`00_java_basics/`](./00_java_basics/README.md) | 阶段 1 | Java 语法与面向对象入门 | ✅ 已生成 |
| [`01_java8_plus/`](./01_java8_plus/README.md) | 阶段 2 | Java 8+（Lambda / Stream 等） | ✅ 已生成 |
| [`02_collections_deep/`](./02_collections_deep/README.md) | 阶段 2 | 集合框架深挖 | ✅ 已生成 |
| [`03_jvm/`](./03_jvm/README.md) | 阶段 3 | JVM 基础 | ✅ 已生成 |
| [`04_concurrency/`](./04_concurrency/README.md) | 阶段 3 | 并发与 JUC | ✅ 已生成 |
| [`05_mysql_jdbc/`](./05_mysql_jdbc/README.md) | 阶段 4 | SQL + JDBC | ✅ 已生成 |
| [`06_spring_boot/`](./06_spring_boot/README.md) | 阶段 5 | Spring Boot Web | ✅ 已生成 |
| [`07_persistence/`](./07_persistence/README.md) | 阶段 5 | JPA / MyBatis | ✅ 已生成 |
| [`08_redis/`](./08_redis/README.md) | 阶段 6 | Redis 缓存 | ✅ 已生成 |
| [`09_message_queue/`](./09_message_queue/README.md) | 阶段 6 | 消息队列 | ✅ 已生成 |
| [`10_microservices/`](./10_microservices/README.md) | 阶段 7 | 微服务入门 | ✅ 已生成 |
| [`11_testing/`](./11_testing/README.md) | 阶段 4～7 穿插 | JUnit / Mockito | ✅ 已生成 |
| [`12_design_patterns/`](./12_design_patterns/README.md) | 阶段 7 | 常用设计模式 | ✅ 已生成 |
| [`13_project_capstone/`](./13_project_capstone/README.md) | 毕业 | 综合小项目 | ✅ 已生成 |
| [`14_spring_boot_prod/`](./14_spring_boot_prod/README.md) | 精通 | Spring Boot 生产级 | ✅ 已生成 |
| [`15_jvm_advanced/`](./15_jvm_advanced/README.md) | 精通 | JVM 进阶 | 📝 生成中 |
| [`16_concurrency_advanced/`](./16_concurrency_advanced/README.md) | 精通 | 并发进阶 | 📝 生成中 |
| [`17_data_infra/`](./17_data_infra/README.md) | 精通 | 数据与中间件 | 📝 生成中 |
| [`18_cloud_native/`](./18_cloud_native/README.md) | 精通 | 云原生入门 | 📝 生成中 |
| [`19_architecture/`](./19_architecture/README.md) | 精通 | 架构与 SRE | 📝 生成中 |

## 环境

- JDK **25 或更新**（本仓库教学与 `pom.xml` 的 `maven.compiler.release` 均按 **25**；生成课可使用 25 的新特性，如未命名类/灵活构造等，但入门课仍以好懂为先）
- 验证入门课（无需 Maven）：

```bash
cd kjv-project
chmod +x scripts/verify_modules.sh
./scripts/verify_modules.sh 00_java_basics
```

- 后续 Spring 等阶段会使用 `./mvnw`（Maven Wrapper，生成该阶段时由 skill 补齐）。

## 一、Java 小白到精通：还该学什么（推荐度从高到低）

下表按**推荐度**从高到低；**目录编号按学习顺序**。推荐度 ≠ 学习顺序（例如 Spring Boot 最值钱，但仍必须先学语法与 SQL）。

判定：必须/强相关就业或生产；可以系统学；有必要（投入产出高于「只刷语法题」）。

| 排名 | 推荐度 | 主题 / 代表资料 | 目录 | 核心内容 | 为什么有必要 |
| --- | --- | --- | --- | --- | --- |
| 1 | ⭐⭐⭐⭐⭐+ | [spring-projects/spring-boot](https://github.com/spring-projects/spring-boot) | `06_spring_boot` | REST、配置、校验、分层、starter | **国内 Java 岗基本盘**。精通目标的主战场；但不要零基础直接跳这里。 |
| 2 | ⭐⭐⭐⭐⭐ | 官方 Oracle / [OpenJDK](https://openjdk.org/) 文档 + 本仓库基础课 | `00_java_basics` | 类型、控制流、OOP、异常、字符串与数组 | **地基**。不会这个，后面全是抄作业。 |
| 3 | ⭐⭐⭐⭐⭐ | MySQL 官方文档 + JDBC | `05_mysql_jdbc` | SQL、索引直觉、事务、JDBC | 业务系统几乎都落库；面试与事故高发区。 |
| 4 | ⭐⭐⭐⭐⭐ | 《深入理解 Java 虚拟机》思路 + 实验 | `03_jvm` | 内存、GC、类加载、常见 OOM | 中高级面试分水岭；线上排障靠它。 |
| 5 | ⭐⭐⭐⭐☆ | JUC / 《Java 并发编程实战》思路 | `04_concurrency` | 线程、池、锁、可见性、常用并发容器 | 高并发与框架底层都绕不过；先会用再谈原理。 |
| 6 | ⭐⭐⭐⭐☆ | Spring Data JPA 或 MyBatis（主攻一个） | `07_persistence` | ORM/映射、仓储、N+1、事务传播 | 不会持久层就做不了真实 CRUD 业务。 |
| 7 | ⭐⭐⭐⭐☆ | Redis | `08_redis` | 缓存、过期、热点、锁入门 | 性能与秒杀/会话场景标配。 |
| 8 | ⭐⭐⭐⭐ | Java 8+ 现代写法 | `01_java8_plus` | Lambda、Stream、Optional、日期、Record | 让你写出当代 Java，而不是 2005 年风格。 |
| 9 | ⭐⭐⭐⭐ | 集合框架深挖 | `02_collections_deep` | ArrayList/HashMap 等选用与坑 | 日写代码最高频；和面试题重合多。 |
| 10 | ⭐⭐⭐⭐ | JUnit 5 + Mockito | `11_testing` | 单元测试、Mock、Spring 切片测试 | 没有测试谈不上精通与团队协作。 |
| 11 | ⭐⭐⭐☆ | 消息队列（RabbitMQ / Kafka 择一） | `09_message_queue` | 异步、削峰、至少一次与幂等 | 业务一复杂就会用到；可在 Spring 之后学。 |
| 12 | ⭐⭐⭐☆ | 微服务（Spring Cloud / Gateway 心智） | `10_microservices` | 拆分、配置、调用、网关 | 大厂常见；小团队可后置，但要懂概念。 |
| 13 | ⭐⭐⭐☆ | 设计模式（结合 Spring） | `12_design_patterns` | 策略、模板、责任链、代理等 | 为了读懂框架与写出可扩展代码，不是背 23 种。 |
| 14 | ⭐⭐⭐ | 综合项目 | `13_project_capstone` | 鉴权 + CRUD + 缓存的完整小服务 | **作品集与验收**；把前面模块串起来。 |
| 15 | ⭐⭐ | 追新框架皮毛 / 过早上 Kubernetes | 不单独建目录 | — | 机会成本高；主线完成前不要分心。 |

## 二、阶段路线（学习顺序）

| 阶段 | 建议周期 | 目录 | 做什么 | 验收标准 |
| --- | --- | --- | --- | --- |
| 1. 语法与 OOP | 2～4 周 | `00_java_basics` | 每天 1～2 课，手敲 + 跑 verify | 不看答案写出类、继承、接口小练习 |
| 2. 现代 Java 与集合 | 2～3 周 | `01_java8_plus`、`02_collections_deep` | Stream 刷题感练习 + 集合选型 | 能用 Stream 做过滤分组；说清 List/Map 选用 |
| 3. JVM 与并发 | 3～5 周 | `03_jvm`、`04_concurrency` | 配合小实验与画图 | 能讲清堆栈与 GC 直觉；会用线程池 |
| 4. 数据与测试 | 2～4 周 | `05_mysql_jdbc`、`11_testing`（开始） | SQL + JDBC + JUnit | 能写带事务的 JDBC 小例子；会为业务方法写测试 |
| 5. Spring 主线 | 1～2 月 | `06_spring_boot`、`07_persistence` | 做一个 CRUD API | 独立完成带校验与统一异常的 REST 服务 |
| 6. 缓存与消息 | 2～4 周 | `08_redis`、`09_message_queue` | 接到上阶段项目上 | 会缓存查询；理解消息异步与幂等 |
| 7. 进阶与作品 | 1～2 月 | `10_`～`13_` | 微服务概念 + 模式 + 毕业项目 | 作品集可演示；简历能写清技术点 |

## 三、用 Cursor 全自动生成（rules + skill）

| 类型 | 路径 | 作用 |
| --- | --- | --- |
| Rule | [.cursor/rules/kjv-learning-standards.mdc](./.cursor/rules/kjv-learning-standards.mdc) | 编号、`L00_` 类名、小白友好、verify |
| Skill | [.cursor/skills/java-study-generator/SKILL.md](./.cursor/skills/java-study-generator/SKILL.md) | 按阶段生成课、模板、分批提交 |

**用法：** Cursor 打开 **`kjv-project` 根目录**，粘贴下面「全自动 Prompt」。

### 推荐粘贴的「全自动」Prompt（复制整段）

```text
请严格按 java-study-generator skill 与 .cursor/rules/kjv-learning-standards.mdc 执行。

我的起点：Java 小白（可能有一点其他语言基础，但请按零基础讲 Java）。
工具链：统一使用 JDK 25 或更新（pom.xml release=25）；生成的代码与讲解按 25，不要按 17 降级。
目标：按 README「阶段路线」顺序，把尚未生成的目录全部自动生成完，使我按顺序学完后达到可就业的 Java 后端水平（语法/集合扎实、懂 JVM 与并发基础、会 MySQL、能独立用 Spring Boot 做中小型 API、会 Redis/MQ 入门、会写测试、有一个综合小项目）。

生成顺序（跳过已完成的课，补齐缺课）：
00_java_basics →
01_java8_plus →
02_collections_deep →
03_jvm →
04_concurrency →
05_mysql_jdbc →
06_spring_boot →
07_persistence →
08_redis →
09_message_queue →
10_microservices →
11_testing →
12_design_patterns →
13_project_capstone

硬性要求：
1. 一次只做一个 NN_xxx；写完更新该阶段 README 与根 README「已生成」表。
2. 每课：L00_Xxx.java 命名、中文注释、直观版+进阶版（有价值时）、runTests()+bootstrap()+main、可用 ./scripts/verify_modules.sh 跑通（无 package 入门课优先）。
3. 每阶段 verify 全绿后单独 git commit；全部完成后 git push。
4. 内容必须由浅入深，禁止默认我已会 Spring/Maven/注解驱动。
5. 不要无关重构；不要 force push。

现在从下一个未完成的阶段开始全自动执行，直到全部完成。
```

## 四、和 kpy / kts 的关系

- `kpy_project`：Python / AI 数据与 Agent
- `kts-project`：TypeScript / RxJS / React 前端
- `kjv-project`：Java 后端主线

三者同级目录，可按兴趣并行，但 **Java 请严格按本仓库阶段顺序**，不要一上来只抄 Spring 教程。
