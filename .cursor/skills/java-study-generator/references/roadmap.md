# kjv-project 阶段路线

## 学习者画像

- **起点**：Java 小白（可能有一点 JS/其他语言基础，但按零基础讲 Java）。
- **目标**：按顺序学完后达到**可就业的 Java 后端**水平（Spring Boot 能独立做中小型业务 API，懂 JVM/并发/MySQL/Redis 基础，会写测试）。

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

推荐度详表见根 `README.md`（推荐度 ≠ 学习顺序：Spring 推荐度最高，但仍必须先学 `00_`～`05_`）。

## 全自动生成批次

1. 一次只做一个 `NN_xxx`。
2. 写课 → 更新阶段 README → verify → 单独 commit。
3. 用户要求再 push。
