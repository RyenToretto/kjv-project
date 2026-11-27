# `17_data_infra` | 数据与中间件深挖

- **目标**: 隔离级别、EXPLAIN、Redis 隐患、投递语义、连接池调优直觉。
- **前置**: `05_`、`08_`、`09_`、`14_`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 17_data_infra`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_isolation/L00_IsolationLevels.java](./00_isolation/L00_IsolationLevels.java) | 脏读与 RC | ✅ |
| `01_` | [01_index_explain/L00_ExplainIntuition.java](./01_index_explain/L00_ExplainIntuition.java) | 迷你 EXPLAIN | ✅ |
| `02_` | [02_redis_hazards/L00_RedisHazards.java](./02_redis_hazards/L00_RedisHazards.java) | 穿透/击穿对策 | ✅ |
| `03_` | [03_mq_semantics/L00_DeliverySemantics.java](./03_mq_semantics/L00_DeliverySemantics.java) | 投递语义与幂等 | ✅ |
| `04_` | [04_pool/L00_ConnectionPoolIdea.java](./04_pool/L00_ConnectionPoolIdea.java) | 连接池借还 | ✅ |

**本目录总课数：5（已完成 5，待生成 0）**
