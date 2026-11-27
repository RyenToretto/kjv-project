# `05_mysql_jdbc` | MySQL 与 JDBC

- **目标**: 建立 SQL/索引/JDBC/事务心智；能用预编译与事务模型思考业务。
- **前置**: `00_java_basics`
- **环境**: JDK 25+；`./scripts/verify_modules.sh 05_mysql_jdbc`
- **说明**: 课内用内存模拟保证离线可跑；建议本机另装 MySQL 8+ 对照练习真实 SQL。

## 章节进度

| 编号 | 文件 | 核心点 | 状态 |
| --- | --- | --- | --- |
| `00_` | [00_sql/L00_SqlBasics.java](./00_sql/L00_SqlBasics.java) | SELECT/WHERE 语义 | ✅ |
| `01_` | [01_index/L00_IndexIntuition.java](./01_index/L00_IndexIntuition.java) | 全表扫描 vs 索引 | ✅ |
| `02_` | [02_jdbc/L00_JdbcPattern.java](./02_jdbc/L00_JdbcPattern.java) | 预编译、防注入、关资源 | ✅ |
| `03_` | [03_tx/L00_TransactionBasics.java](./03_tx/L00_TransactionBasics.java) | 提交/回滚与原子性 | ✅ |

**本目录总课数：4（已完成 4，待生成 0）**
