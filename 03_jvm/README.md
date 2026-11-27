# `03_jvm` | JVM 基础

- **目标**: 建立堆/栈、GC 可达性、类加载与常见 OOM 排查直觉。
- **前置**: `00_java_basics`、`02_collections_deep`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 03_jvm`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_memory/L00_MemoryModel.java](./00_memory/L00_MemoryModel.java) | 栈帧 vs 堆、引用别名 | ✅ |
| `01_` | [01_gc/L00_GcIntuition.java](./01_gc/L00_GcIntuition.java) | 可达性、强/弱引用 | ✅ |
| `02_` | [02_classloader/L00_ClassLoading.java](./02_classloader/L00_ClassLoading.java) | 初始化时机、Class.forName | ✅ |
| `03_` | [03_troubleshooting/L00_CommonOomPatterns.java](./03_troubleshooting/L00_CommonOomPatterns.java) | SO/OOM 分类与排查心智 | ✅ |

**本目录总课数：4（已完成 4，待生成 0）**
