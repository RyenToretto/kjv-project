# `14_spring_boot_prod` | Spring Boot 生产级心智

- **目标**: 把 `13` 迷你商店升级为生产分层：JWT、事务边界、缓存接入、统一错误、切片测试。
- **前置**: `06_`～`13_`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 14_spring_boot_prod`
- **说明**: 本阶段仍用纯 JDK 可运行骨架对照 Spring Boot 注解与组件；落地真项目时用 start.spring.io 创建 Boot 3.x 并一一映射本课类。

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_layering/L00_LayeredArchitecture.java](./00_layering/L00_LayeredArchitecture.java) | Controller/Service/Repo/DTO | ✅ |
| `01_` | [01_security_jwt/L00_JwtAuthFilter.java](./01_security_jwt/L00_JwtAuthFilter.java) | JWT 签发校验与 Filter | ✅ |
| `02_` | [02_jpa_tx/L00_TransactionalBoundary.java](./02_jpa_tx/L00_TransactionalBoundary.java) | `@Transactional` 边界 | ✅ |
| `03_` | [03_cache_integration/L00_ServiceCacheIntegration.java](./03_cache_integration/L00_ServiceCacheIntegration.java) | `@Cacheable` / Evict | ✅ |
| `04_` | [04_api_errors/L00_ProblemDetails.java](./04_api_errors/L00_ProblemDetails.java) | 统一错误体 | ✅ |
| `05_` | [05_testing_slice/L00_SliceTestStyle.java](./05_testing_slice/L00_SliceTestStyle.java) | MockMvc 切片测试 | ✅ |

**本目录总课数：6（已完成 6，待生成 0）**
