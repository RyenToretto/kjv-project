# kjv-project 阶段路线

## 学习者画像

- **起点**：Java 小白（可能有一点 JS/其他语言基础，但按零基础讲 Java）。
- **目标**：按顺序学完后达到**可就业的 Java 后端**水平，并继续 `14_`～`19_` 冲刺各方向精通。
- **JDK**：统一 **JDK 25 或更新**（`pom.xml` 的 `maven.compiler.release=25`）。

## 阶段编号（学习顺序）

| 编号 | 目录 | 内容 |
| --- | --- | --- |
| `00_` | `00_java_basics` | 语法、类型、控制流、方法、数组、OOP、异常、常用 API |
| `01_` | `01_java8_plus` | Lambda、Stream、Optional、日期 API、Record、变体 |
| `02_` | `02_collections_deep` | List/Map/Set 选用、底层直觉、equals/hashCode |
| `03_` | `03_jvm` | 内存区域、GC 直觉、类加载、常见排查 |
| `04_` | `04_concurrency` | Thread、池化、JUC、可见性与锁 |
| `05_` | `05_mysql_jdbc` | SQL 基础、索引直觉、JDBC、事务 |
| `06_` | `06_spring_boot` | REST、配置、校验、异常处理、分层 |
| `07_` | `07_persistence` | Spring Data JPA / MyBatis 择一主攻 + 另一了解 |
| `08_` | `08_redis` | 缓存、分布式锁入门、过期与一致性直觉 |
| `09_` | `09_message_queue` | 异步解耦、至少一种 MQ 心智模型 |
| `10_` | `10_microservices` | 网关、配置、服务间调用、幂等 |
| `11_` | `11_testing` | JUnit5、Mockito、切片测试 |
| `12_` | `12_design_patterns` | 常用模式与 Spring 中的体现 |
| `13_` | `13_project_capstone` | 综合小项目（CRUD + 鉴权 + 缓存） |
| `14_` | `14_spring_boot_prod` | 生产分层、JWT、事务、缓存、错误体、切片测试 |
| `15_` | `15_jvm_advanced` | 双亲委派、GC 选型、泄漏、诊断、JIT |
| `16_` | `16_concurrency_advanced` | AQS、CF、虚拟线程、CAS、池调参 |
| `17_` | `17_data_infra` | 隔离级别、EXPLAIN、Redis 隐患、投递语义、连接池 |
| `18_` | `18_cloud_native` | 网关、配置中心、发现、追踪、K8s 探针 |
| `19_` | `19_architecture` | DDD、事件、CQRS、ADR、SRE 门禁 |

精通推荐学习顺序（与编号不同）：`14 → 17 → 16 → 15 → 18 → 19`。

## 全自动生成批次

1. 一次只做一个 `NN_xxx`。
2. 写课 → 更新阶段 README → verify → 单独 commit。
3. 用户要求再 push。
