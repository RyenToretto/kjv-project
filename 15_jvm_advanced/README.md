# `15_jvm_advanced` | JVM 进阶

- **目标**: 双亲委派、GC 选型、泄漏模式、诊断手册、JIT/逃逸分析直觉。
- **前置**: `03_jvm`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 15_jvm_advanced`

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_classloader/L00_ParentDelegation.java](./00_classloader/L00_ParentDelegation.java) | 双亲委派模拟 | ✅ |
| `01_` | [01_gc_compare/L00_GcCollectorCompare.java](./01_gc_compare/L00_GcCollectorCompare.java) | G1/ZGC 选型 | ✅ |
| `02_` | [02_leak/L00_MemoryLeakPatterns.java](./02_leak/L00_MemoryLeakPatterns.java) | 泄漏模式 | ✅ |
| `03_` | [03_diagnostics/L00_JvmDiagnosticsPlaybook.java](./03_diagnostics/L00_JvmDiagnosticsPlaybook.java) | MXBean + jcmd 清单 | ✅ |
| `04_` | [04_jit/L00_JitAndEscapeIdea.java](./04_jit/L00_JitAndEscapeIdea.java) | JIT/分配直觉 | ✅ |

**本目录总课数：5（已完成 5，待生成 0）**
