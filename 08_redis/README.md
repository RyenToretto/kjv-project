# `08_redis` | Redis 缓存

- **目标**: 掌握 Cache-Aside、TTL/雪崩/穿透直觉、分布式锁入门。
- **前置**: `06_spring_boot`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 08_redis`
- **说明**: 课内用内存模拟 Redis；接真 Redis 时对照 GET/SET/DEL/SET NX。

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_cache/L00_CacheAside.java](./00_cache/L00_CacheAside.java) | 缓存旁路读写 | ✅ |
| `01_` | [01_expire/L00_ExpireAndStampede.java](./01_expire/L00_ExpireAndStampede.java) | TTL、抖动、空值 | ✅ |
| `02_` | [02_lock/L00_DistributedLockIntro.java](./02_lock/L00_DistributedLockIntro.java) | NX 锁与 token 释放 | ✅ |

**本目录总课数：3（已完成 3，待生成 0）**
