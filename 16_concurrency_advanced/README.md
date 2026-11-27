# `16_concurrency_advanced` | 并发进阶

- **目标**: AQS 直觉、CompletableFuture 编排、虚拟线程、CAS 无锁、线程池调参。
- **前置**: `04_concurrency`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 16_concurrency_advanced`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_aqs/L00_AqsIntuition.java](./00_aqs/L00_AqsIntuition.java) | 迷你独占锁 / park | ✅ |
| `01_` | [01_completable/L00_CompletableFutureOrchestration.java](./01_completable/L00_CompletableFutureOrchestration.java) | 并行编排与恢复 | ✅ |
| `02_` | [02_virtual_threads/L00_VirtualThreads.java](./02_virtual_threads/L00_VirtualThreads.java) | 虚拟线程 | ✅ |
| `03_` | [03_cas/L00_CasAndLockFree.java](./03_cas/L00_CasAndLockFree.java) | CAS / 无锁栈 | ✅ |
| `04_` | [04_pool_tuning/L00_ThreadPoolTuning.java](./04_pool_tuning/L00_ThreadPoolTuning.java) | 有界队列与拒绝策略 | ✅ |

**本目录总课数：5（已完成 5，待生成 0）**
