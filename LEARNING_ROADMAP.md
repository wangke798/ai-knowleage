# 后端技术栈学习路线

## 技术全景

```
Spring Boot 3.3.5（核心基础）
├── Spring Security + JWT          认证鉴权
├── MyBatis-Plus 3.5.7             数据访问
│   ├── Dynamic DataSource         多数据源路由（MySQL + PostgreSQL）
│   └── Flyway                     数据库版本迁移
├── Spring AI 1.0.0                AI 核心框架
│   ├── PGVector                   向量存储与相似度检索
│   ├── RAG 流程                   检索增强生成
│   ├── HyDE                       假设文档增强检索
│   └── Rerank                     关键词重排序
├── Redis                          缓存 + 限流
├── MinIO                          对象存储（文件上传）
├── Apache Tika 2.9.2              文档解析（PDF / Word / TXT）
├── Bucket4j 8.10.1                令牌桶限流
├── MapStruct 1.6.2                DTO ↔ Entity 映射
├── Knife4j 4.5.0                  OpenAPI 3 接口文档
└── Java 21 虚拟线程               高并发异步处理
```

---

## 第一阶段：基础框架（第 1-2 周）

**目标**：能看懂项目启动流程、接口请求链路

| 优先级 | 技术 | 对应代码 | 学习要点 |
|---|---|---|---|
| ⭐⭐⭐ | **Spring Boot 3.x** | `AiKnowledgeBaseApplication.java`、所有 `@Configuration` | 自动配置、Bean 生命周期、`application.yml` 结构 |
| ⭐⭐⭐ | **Spring MVC** | `module/*/controller/` | `@RestController`、统一响应 `Result<T>`、全局异常处理 |
| ⭐⭐⭐ | **Lombok** | 所有 Entity / DTO | `@Data`、`@Builder`、`@Slf4j` |
| ⭐⭐ | **MapStruct** | `module/*/` 中的 Mapper 接口 | DTO ↔ Entity 转换原理 |
| ⭐⭐ | **Maven** | `pom.xml` | BOM 依赖管理、annotationProcessorPaths |

**建议阅读顺序**：

1. `resources/application.yml` → 理解整体配置结构
2. `common/result/` → 统一返回体 `Result<T>`
3. `common/exception/` → 全局异常处理器
4. `module/user/controller/` → 最简单的 CRUD 接口示例

---

## 第二阶段：安全与数据库（第 3-4 周）

**目标**：理解登录流程和数据读写机制

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **Spring Security + JWT** | `security/` 全部、`config/SecurityConfig.java` | Filter 链、`JwtAuthenticationFilter`、无状态鉴权 |
| **MyBatis-Plus** | `module/*/mapper/`、`module/*/entity/`、`config/MybatisPlusConfig.java` | `BaseMapper`、`LambdaQueryWrapper`、逻辑删除、分页插件 |
| **Dynamic DataSource** | `config/DataSourceConfig.java`、`@DS` 注解 | MySQL 主库 + PostgreSQL 副库路由切换原理 |
| **Flyway** | `resources/db/migration/V*.sql` | 版本化迁移脚本、`repair()` 策略 |

**建议阅读顺序**：

1. `security/JwtTokenProvider.java` → Token 生成与解析
2. `security/JwtAuthenticationFilter.java` → 请求拦截流程
3. `config/SecurityConfig.java` → 路由白名单配置
4. `module/user/` 完整模块 → 注册 / 登录全链路
5. `module/kb/mapper/` → MyBatis-Plus 查询示例

---

## 第三阶段：存储与文档处理（第 5 周）

**目标**：理解文件上传到向量入库的前半段流程

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **MinIO** | `module/kb/storage/`、`config/UploadProperties.java` | Bucket 操作、预签名 URL、文件流上传 |
| **Apache Tika** | `module/kb/service/DocumentParseService.java` | 自动检测文件类型、PDF / Word / TXT 文本抽取 |
| **Redis** | `config/`、各 Service 中的缓存注解 | `@Cacheable`、`RedisTemplate`、Bucket4j 令牌桶限流 |

**建议阅读顺序**：

1. `DocumentParseService.java` → 文件如何转换为纯文本
2. `DocumentService.java` → 上传 → 解析 → 分块完整流程
3. `module/kb/storage/` → MinIO 操作封装

---

## 第四阶段：Spring AI 与 RAG 核心（第 6-9 周）⭐ 重点

**目标**：掌握本项目最核心的 AI 检索增强生成能力

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **Spring AI 基础** | `config/AiConfig.java`、`config/PgVectorConfig.java` | `ChatClient`、`EmbeddingModel`、多模型动态切换 |
| **PGVector 向量存储** | `config/PgVectorConfig.java`、`module/kb/service/` | 文本 → 向量 → 存储 → 余弦相似度检索 |
| **RAG 完整流程** | `module/chat/service/impl/RagChatServiceImpl.java` | 问题向量化 → 检索 Top-K → 构建 Prompt → 流式输出 |
| **HyDE 检索增强** | `module/kb/service/HydeRetrievalService.java` | 先让 LLM 生成假设文档，再用假设文档向量检索，提升召回率 |
| **Rerank 重排序** | `module/kb/service/RerankService.java`、`impl/KeywordRerankServiceImpl.java` | BM25 / 关键词重排序，提升检索精度 |
| **SSE 流式输出** | `module/chat/controller/`、前端 `src/lib/sse.ts` | Server-Sent Events 实现打字机效果 |

**建议阅读顺序**：

1. `config/AiConfig.java` → 了解模型如何注入 Spring 容器
2. `config/PgVectorConfig.java` → 向量库连接与维度配置
3. `RagChatServiceImpl.java` → **最核心文件**，整个 RAG 链路的入口
4. `HydeRetrievalService.java` → HyDE 增强检索原理
5. `RerankService.java` → 重排序逻辑
6. Chat Controller → SSE 流式接口实现

### RAG 流程图

```
用户提问
   │
   ▼
[EmbeddingModel] 问题向量化
   │
   ├─(HyDE) LLM 生成假设文档 → 假设文档向量化
   │
   ▼
[PGVector] 相似度检索 Top-K 文档块
   │
   ▼
[RerankService] 关键词重排序
   │
   ▼
[ChatClient] 拼装 System Prompt + 检索上下文 + 用户问题
   │
   ▼
[SSE] 流式返回 Token 给前端
```

---

## 第五阶段：工程化实践（第 10 周）

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **Java 21 虚拟线程** | `config/AsyncConfig.java`、`application.yml` | `spring.threads.virtual.enabled=true`、高并发轻量线程 |
| **Bucket4j 限流** | 接口层注解或拦截器 | 令牌桶算法、每用户 / 每接口限流策略 |
| **Knife4j API 文档** | 各 Controller 的 `@Operation` 注解 | OpenAPI 3 规范、接口文档自动生成与调试 |
| **Docker Compose** | `deploy/docker-compose.yml` | MySQL + PostgreSQL + Redis + MinIO 一键启动 |

---

## 总体时间规划

| 周次 | 内容 | 预计用时 |
|---|---|---|
| 第 1-2 周 | Spring Boot 基础 + MVC + Lombok + Maven | 2 周 |
| 第 3-4 周 | Spring Security + JWT + MyBatis-Plus + Flyway | 2 周 |
| 第 5 周 | MinIO + Apache Tika + Redis 缓存 | 1 周 |
| 第 6-7 周 | Spring AI 基础 + PGVector + RAG 主流程 | 2 周 |
| 第 8-9 周 | HyDE + Rerank + SSE 流式输出 | 2 周 |
| 第 10 周 | 虚拟线程 + 限流 + Docker 部署 | 1 周 |

> **总计约 10 周**，每天 1.5 ~ 2 小时。
> 第 6-9 周的 Spring AI + RAG 部分是本项目最核心的技术价值，建议投入最多时间与精力。

---

## 推荐参考资料

| 技术 | 推荐资料 |
|---|---|
| Spring Boot 3 | [官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/) |
| Spring Security | [官方文档](https://docs.spring.io/spring-security/reference/) |
| MyBatis-Plus | [官方文档](https://baomidou.com/) |
| Spring AI | [官方文档](https://docs.spring.io/spring-ai/reference/) |
| PGVector | [pgvector GitHub](https://github.com/pgvector/pgvector) |
| RAG 原理 | [Retrieval-Augmented Generation 论文](https://arxiv.org/abs/2005.11401) |
| HyDE 原理 | [HyDE 论文](https://arxiv.org/abs/2212.10496) |
| MinIO | [官方文档](https://min.io/docs/minio/linux/index.html) |
| Apache Tika | [官方文档](https://tika.apache.org/) |
