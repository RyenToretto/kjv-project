# `04_concurrency` | 并发与 JUC

- **目标**: 会起线程、理解竞态与 synchronized、会用 JUC 常用工具与线程池。
- **前置**: `00_java_basics`、`03_jvm`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 04_concurrency`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_thread/L00_ThreadBasics.java](./00_thread/L00_ThreadBasics.java) | Thread / Runnable / join | ✅ |
| `01_` | [01_sync/L00_SyncAndVisibility.java](./01_sync/L00_SyncAndVisibility.java) | 竞态、synchronized、Atomic | ✅ |
| `02_` | [02_juc/L00_JucTools.java](./02_juc/L00_JucTools.java) | ConcurrentHashMap、Latch、Lock | ✅ |
| `03_` | [03_pool/L00_ThreadPool.java](./03_pool/L00_ThreadPool.java) | ExecutorService 固定池 | ✅ |

**本目录总课数：4（已完成 4，待生成 0）**
