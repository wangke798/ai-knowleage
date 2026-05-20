# AI 知识库项目 — 开发计划

> 一个基于 RAG（Retrieval-Augmented Generation，检索增强生成）的 AI 知识库系统，采用前后端分离架构。
>
> - 创建日期：2026-05-18
> - 项目根目录：`/Users/luke/projects`
> - 后端项目：`ai-knowledge-base-backend`（已初始化）
> - 前端项目：`ai-knowledge-base-frontend`（待创建）

---

## 一、项目目标

打造一个面向团队/个人的 **AI 知识库平台**，核心能力：

1. **文档管理**：上传 PDF / Word / Markdown / TXT / 网页等多格式文档。
2. **智能解析**：文档自动解析、分块（chunking）、向量化（embedding）入库。
3. **语义检索**：基于向量相似度的检索 + 关键词混合检索。
4. **RAG 问答**：结合检索结果与大模型，实现可溯源的智能问答（带引用）。
5. **多知识库**：支持创建多个知识库（按团队/项目/主题隔离）。
6. **会话历史**：对话上下文管理、历史记录、收藏。
7. **权限管理**：用户/角色/知识库三级权限控制。

---

## 二、整体架构

```
┌──────────────────────────────────────────────────────────────┐
│                       前端 (React + Vite)                    │
│  - 知识库管理  - 文档上传  - 对话窗口(流式)  - 用户中心       │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTPS / SSE
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                  后端 API (Spring Boot 3 + Spring AI)         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐    │
│  │ 用户/鉴权模块 │  │ 文档处理模块  │  │ RAG 对话模块      │    │
│  │ (JWT/RBAC)   │  │ (解析/分块)   │  │ (检索/生成/流式)  │    │
│  └──────────────┘  └──────────────┘  └──────────────────┘    │
└──────┬─────────────────────┬───────────────────────┬─────────┘
       │                     │                       │
       ▼                     ▼                       ▼
   ┌────────┐         ┌─────────────┐         ┌──────────────┐
   │ MySQL  │         │  MinIO /    │         │  向量库       │
   │ (业务) │         │  本地文件    │         │  (PGVector/   │
   │        │         │  (原文件)   │         │   Milvus)    │
   └────────┘         └─────────────┘         └──────────────┘
       │                                              │
       ▼                                              ▼
   ┌────────┐                              ┌──────────────────┐
   │ Redis  │                              │  LLM / Embedding │
   │(缓存)  │                              │  (OpenAI/通义/   │
   │        │                              │   Ollama 本地)   │
   └────────┘                              └──────────────────┘
```

---

## 三、技术栈

### 3.1 后端（`ai-knowledge-base-backend`）

| 类别 | 技术选型 | 说明 |
| --- | --- | --- |
| 语言 / 运行时 | **Java 21** | LTS，使用虚拟线程提升 IO 吞吐 |
| 基础框架 | **Spring Boot 3.3.5** | 已配置 |
| AI 集成 | **Spring AI 1.0.x 正式版** | 统一接入 LLM / Embedding / VectorStore；⚠️ 锁版本，M 版 API 有破坏性变更 |
| Web | Spring Web + **SseEmitter**（流式输出） | SSE 推送对话；勿同时引入 WebFlux（Servlet/Reactive 容器冲突） |
| 安全 | Spring Security + **JJWT 0.12.6** | JWT 鉴权 + RBAC |
| 数据 ORM | **MyBatis-Plus 3.5.7** | 业务数据 CRUD |
| 数据库迁移 | **Flyway** | 版本化管理 schema 变更（`V1__init.sql`、`V2__xxx.sql`） |
| 关系数据库 | **MySQL 8** | 用户、知识库、文档元数据 |
| 缓存 | **Redis 7** | Token 黑名单、对话上下文、限流 |
| 向量数据库 | **PGVector**（推荐起步）/ Milvus / Elasticsearch 8 | 文档向量存储与检索 |
| 对象存储 | **MinIO**（自部署）/ 阿里云 OSS | 原始文件存储 |
| 文档解析 | **Apache Tika** + **PDFBox** + `docx4j` | 多格式文本提取 |
| 文本分块 | Spring AI `TokenTextSplitter` | 按 token 切分 |
| LLM 提供商 | OpenAI / 通义千问 / DeepSeek / **Ollama（本地）** | 多模型可切换 |
| Embedding 模型 | `text-embedding-3-small` / `bge-m3` / `nomic-embed-text` | 中文优先 bge-m3 |
| 工具库 | Hutool、Lombok、MapStruct | 已部分配置 |
| API 文档 | **Knife4j 4.5.0** | 已配置 |
| 任务调度 | Spring `@Scheduled` / XXL-Job（可选） | 文档异步处理 |
| 消息队列 | RabbitMQ / Redis Stream（可选） | 文档解析任务解耦 |
| 监控 | Spring Boot Actuator + Prometheus + Grafana | 生产环境 |
| 日志 | Logback + ELK（可选） | 结构化日志 |
| 构建 / 部署 | Maven + Docker + Docker Compose | 一键启动 |
| 测试 | JUnit 5 + Mockito + Testcontainers | 单元 + 集成测试 |

### 3.2 前端（`ai-knowledge-base-frontend`，待创建）

| 类别 | 技术选型 | 说明 |
| --- | --- | --- |
| 框架 | **React 18+** (函数组件 + Hooks) | 主框架 |
| 构建工具 | **Vite 5** | 极速热更新、开箱即用 TS 支持 |
| 语言 | **TypeScript 5** | 类型安全 |
| 路由 | **React Router 6** | SPA 路由 |
| 状态管理 | **Zustand** | 轻量级全局状态，替代 Redux |
| 请求 / 缓存 | **TanStack Query (React Query) v5** | 服务端状态、缓存、自动重试、轮询 |
| HTTP 客户端 | **Axios** + 拦截器 | 携带 JWT、统一错误处理（被 TanStack Query 包装） |
| UI 组件库 | **shadcn/ui** | 基于 Radix UI + Tailwind，可复制可定制 |
| CSS | **TailwindCSS** | 原子化样式（shadcn/ui 配套） |
| 流式通信 | `@microsoft/fetch-event-source` | SSE 接收 AI 流式回复（支持 POST + Header） |
| Markdown 渲染 | **react-markdown** + `remark-gfm` + `rehype-highlight` + `rehype-katex` | AI 回答渲染（代码高亮、表格、公式） |
| 表单 | React Hook Form + Zod | 表单校验 |
| 图标 | `lucide-react` | shadcn/ui 默认图标库 |
| 工具库 | clsx + tailwind-merge（`cn` 工具）、date-fns | |
| 代码规范 | ESLint + Prettier + Husky + lint-staged | Git 提交检查 |
| 单元测试 | Vitest + React Testing Library | |
| E2E 测试 | Playwright（可选） | |
| 包管理 | **pnpm** | 推荐 |

### 3.3 基础设施

| 组件 | 部署方式 |
| --- | --- |
| MySQL 8 | Docker Compose |
| Redis 7 | Docker Compose |
| PostgreSQL + PGVector | Docker Compose |
| MinIO | Docker Compose |
| Ollama（可选） | 本地安装，跑 `bge-m3` / `qwen2.5` |
| Nginx | 前端静态资源 + 反向代理后端 |

---

## 四、后端模块设计

```
com.smartdocs.aikb
├── common              # 通用：Result<T> 统一响应、全局 @RestControllerAdvice、TraceId(MDC)、常量、工具
├── config              # 配置类：Security、AI、Redis、Mybatis、CORS、Knife4j
├── security            # JWT 过滤器、UserDetailsService、权限注解
├── module
│   ├── user            # 用户、角色、权限
│   ├── kb              # 知识库（KnowledgeBase）CRUD、成员、配额
│   ├── document        # 文档：上传、解析、分块、向量化、状态机
│   ├── chat            # 会话、消息、流式回答、引用溯源
│   ├── retrieval       # 向量检索 + 关键词检索 + 重排序
│   └── system          # 字典、日志、模型配置
├── ai
│   ├── llm             # ChatClient 封装、多模型路由
│   ├── embedding       # Embedding 客户端封装
│   ├── vectorstore     # VectorStore 适配（PGVector / Milvus）
│   ├── splitter        # 自定义分块策略
│   └── prompt          # PromptTemplate 集中管理
└── AiKnowledgeBaseApplication.java
```

### 关键数据表（MySQL）

- `sys_user` / `sys_role` / `sys_user_role` / `sys_permission`
- `kb_knowledge_base`：知识库主表
- `kb_member`：知识库成员（用户-知识库-角色）
- `kb_document`：文档元数据（名称、大小、状态：待解析/解析中/已完成/失败、`file_hash` SHA-256 去重、`version`、`parent_doc_id`）
- `kb_document_chunk`：文档分块（chunk_id 与向量库一一对应）
- `chat_conversation`：会话
- `chat_message`：消息（含引用 chunk ids）
- `sys_model_config`：模型配置（API Key、URL、模型名）

向量库（PGVector）存储：`{chunk_id, kb_id, doc_id, embedding(vector), content, metadata}`

---

## 五、核心流程

### 5.1 文档入库（异步）

```
上传 → 存 MinIO → 写 kb_document (状态=待解析)
   → MQ/线程池触发解析任务
   → Tika 提取文本 → 清洗 → TokenTextSplitter 分块
   → 调用 Embedding API → 写入 PGVector + kb_document_chunk
   → 更新状态=已完成（失败则记录错误）
```

### 5.2 RAG 问答（流式）

```
用户提问 → 改写查询(可选) → Embedding 化
   → 向量库 TopK 检索 + 关键词检索 → Rerank (可选)
   → 拼装 Prompt (上下文 + 历史 + 问题)
   → ChatClient.stream() → SSE 推送给前端
   → 落库 chat_message + 引用 chunk_ids
```

---

## 六、开发计划（里程碑）

### Phase 0 — 基础设施（1～2 天）
- [ ] 创建 `ai-knowledge-base-frontend`（Vite + React + TS + Tailwind + shadcn/ui）
- [ ] 初始化 TanStack Query、Zustand、React Router、Axios 拦截器
- [ ] 编写 `docker-compose.yml`：MySQL、Redis、PostgreSQL+PGVector、MinIO
- [ ] 后端配置 `application.yml`（多 profile：dev / prod）
- [ ] 引入 **Flyway**，编写 `V1__init.sql` 初始化所有表结构（替代手动 schema.sql）
- [ ] 定义 `Result<T>` 统一响应结构 + 全局 `@RestControllerAdvice`（业务异常、参数校验、Security 异常）
- [ ] MDC 注入 TraceId，Axios 请求头携带 `X-Trace-Id`，贯穿前后端日志

### Phase 1 — 用户与权限（2～3 天）
- [ ] 用户注册 / 登录 / JWT 颁发：Access Token（短期 15 min）+ Refresh Token（长期，HttpOnly Cookie），Token 黑名单存 Redis
- [ ] Spring Security 过滤器链 + Knife4j 白名单（`/doc.html`、`/v3/api-docs/**`、`/webjars/**`）
- [ ] RBAC 模型 + `@PreAuthorize`
- [ ] 前端登录页 + 受保护路由 (`<ProtectedRoute>`) + Token 管理（Zustand persist）

### Phase 2 — 知识库管理（2 天）
- [ ] 知识库 CRUD + 成员管理
- [ ] 前端知识库列表 / 详情 / 设置页

### Phase 3 — 文档管理与解析（3～4 天）
- [ ] 文件上传（MinIO 接入）+ 安全校验（MIME 白名单、大小限制、SHA-256 指纹去重）
- [ ] 文档版本管理：重复上传生成新版本，旧 chunks 软删除（`file_hash` / `version` / `parent_doc_id`）
- [ ] Apache Tika 文本提取
- [ ] 自定义分块策略：先按标点/段落语义分句，再按 token 上限切分（替代纯 `TokenTextSplitter`）
- [ ] Embedding 接入 + **父子分块**写入 PGVector（小 chunk 精准检索，大 chunk 喂 LLM 保留上下文）
- [ ] 文档状态机 + 异步任务
- [ ] 前端：上传组件、进度展示、文档列表

### Phase 4 — 检索与 RAG 问答（3～5 天）
- [ ] 多轮 **Query 改写**：用小模型将含上下文依赖的问题改写为可独立检索的查询
- [ ] 向量检索 + 关键词混合检索
- [ ] Prompt 模板设计：含引用要求 + **Prompt 注入防护**（用户输入用明确分隔符包裹，系统提示约束"无依据则不回答"）
- [ ] `ChatClient` 流式输出（SSE）+ 对 `/chat/**` 接口按用户限流（Bucket4j）
- [ ] 会话/消息持久化 + 多轮上下文（强制截断历史，避免 Token 超限）
- [ ] 前端：对话窗口、SSE 流式渲染（攒 16 ms 批量 flush 再 setState，避免渲染抖动）、`react-markdown` 渲染（代码高亮 / KaTeX）、引用气泡

### Phase 4.5 — RAG 评测（1～2 天，每次大改后复跑）
- [ ] 构建黄金问答集（20～50 条：question / expected_answer / expected_chunk_ids）
- [ ] 检索召回率 Recall@K：期望 chunk 是否出现在 TopK 结果中
- [ ] 回答忠实度 Faithfulness + 回答相关性 Answer Relevance（Ragas 或 LLM-as-Judge）
- [ ] 每次修改 Prompt / 分块策略 / Embedding 模型后必须跑一次，量化对比迭代效果

### Phase 5 — 增强能力（按需）
- [ ] 多模型切换（OpenAI / 通义 / Ollama）
- [ ] Rerank 重排（bge-reranker）
- [ ] **HyDE**：先让 LLM 生成"假设答案"再向量化检索，提升召回率
- [ ] 问答 **Redis 缓存**：相同问题 + 相同 KB 命中缓存（关键词归一化后 SHA-256 做 key）
- [ ] 引用溯源高亮（点击跳转原文）
- [ ] 文档预览（PDF.js）
- [ ] 前端文档/对话列表**虚拟滚动**（`@tanstack/react-virtual`，条目 > 500 时必需）
- [ ] 历史对话搜索、收藏、导出
- [ ] 知识库统计仪表盘

### Phase 6 — 工程化与上线（2～3 天）
- [ ] 单元测试 + 集成测试（Testcontainers）
- [ ] CI/CD（GitHub Actions / GitLab CI）
- [ ] Dockerfile（前后端） + 生产 `docker-compose`
- [ ] Nginx 反代 + HTTPS
- [ ] Prometheus + Grafana 监控
- [ ] 操作手册 + API 文档归档

---

## 七、目录规划

```
/Users/luke/projects
├── DEVELOPMENT_PLAN.md
├── ai-knowledge-base-backend/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── pom.xml
├── ai-knowledge-base-frontend/      ← 见 7.1 详细结构
└── deploy/
    ├── docker-compose.yml
    ├── nginx/
    └── sql/
```

### 7.1 前端目录详细结构（Feature-Based）

> 设计原则：**业务逻辑内聚在 `features/`，只有真正跨模块复用的才提升到全局层。**

```
ai-knowledge-base-frontend/
├── public/
├── src/
│   │
│   ├── app/                        # 应用入口，只做"组装"
│   │   ├── App.tsx
│   │   ├── router.tsx              # 路由表（见 7.2 路由结构）
│   │   └── providers.tsx           # QueryClientProvider、主题等全局 Provider
│   │
│   ├── components/                 # 与业务无关的纯通用组件
│   │   ├── ui/                     # shadcn/ui 生成（Button、Dialog、Input…）
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx       # 主布局：侧边栏 + Header + <Outlet>
│   │   │   ├── AuthLayout.tsx      # 登录/注册布局：居中卡片
│   │   │   ├── Sidebar.tsx         # 左侧导航（知识库列表、全局菜单）
│   │   │   └── Header.tsx          # 顶栏（面包屑、用户头像、通知）
│   │   └── shared/                 # 业务通用但跨 feature 复用
│   │       ├── ConfirmDialog.tsx
│   │       ├── EmptyState.tsx
│   │       ├── PageHeader.tsx
│   │       └── StatusBadge.tsx
│   │
│   ├── features/                   # 核心：按业务功能模块划分
│   │   │
│   │   ├── auth/                   # 认证模块
│   │   │   ├── api.ts              # POST /auth/login、/register、/refresh
│   │   │   ├── components/
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   └── RegisterForm.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useAuth.ts      # useLogin / useRegister / useLogout
│   │   │   └── pages/
│   │   │       ├── LoginPage.tsx
│   │   │       └── RegisterPage.tsx
│   │   │
│   │   ├── knowledge-base/         # 知识库模块
│   │   │   ├── api.ts
│   │   │   ├── components/
│   │   │   │   ├── KbCard.tsx      # 知识库卡片
│   │   │   │   ├── KbCreateDialog.tsx
│   │   │   │   └── MemberTable.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useKnowledgeBases.ts
│   │   │   │   └── useKbMembers.ts
│   │   │   └── pages/
│   │   │       ├── KbListPage.tsx      # /kb — 知识库列表（首页）
│   │   │       ├── KbDetailPage.tsx    # /kb/:kbId — 详情+文档列表 Tab
│   │   │       └── KbSettingsPage.tsx  # /kb/:kbId/settings
│   │   │
│   │   ├── document/               # 文档模块
│   │   │   ├── api.ts
│   │   │   ├── components/
│   │   │   │   ├── DocUploader.tsx     # 拖拽上传 + 进度条
│   │   │   │   ├── DocList.tsx         # 文档列表（含解析状态轮询）
│   │   │   │   ├── DocStatusBadge.tsx  # 待解析/解析中/已完成/失败
│   │   │   │   └── ChunkViewer.tsx     # 分块内容查看（调试用）
│   │   │   ├── hooks/
│   │   │   │   ├── useDocuments.ts
│   │   │   │   └── useUpload.ts
│   │   │   └── pages/
│   │   │       └── DocDetailPage.tsx   # /kb/:kbId/docs/:docId — 预览+分块列表
│   │   │
│   │   ├── chat/                   # 对话模块（最复杂）
│   │   │   ├── api.ts
│   │   │   ├── components/
│   │   │   │   ├── ConversationList.tsx  # 左侧历史会话列表
│   │   │   │   ├── MessageBubble.tsx     # 单条消息（用户/AI 区分样式）
│   │   │   │   ├── AiMessage.tsx         # AI 消息：react-markdown 渲染 + 打字机效果
│   │   │   │   ├── CitationCard.tsx      # 引用溯源气泡（点击跳转原文）
│   │   │   │   └── ChatInput.tsx         # 输入框 + 发送 + 停止按钮
│   │   │   ├── hooks/
│   │   │   │   ├── useConversations.ts   # TanStack Query：会话列表
│   │   │   │   └── useChat.ts            # 管理 SSE 流式状态（streaming/error/done）
│   │   │   └── pages/
│   │   │       └── ChatPage.tsx          # /kb/:kbId/chat
│   │   │
│   │   └── settings/               # 设置模块
│   │       ├── api.ts
│   │       ├── components/
│   │       └── pages/
│   │           ├── ProfilePage.tsx     # /settings/profile
│   │           ├── SecurityPage.tsx    # /settings/security（修改密码）
│   │           └── ModelConfigPage.tsx # /settings/models（管理员：配置 LLM/Embedding）
│   │
│   ├── hooks/                      # 全局通用 hooks
│   │   ├── useDebounce.ts
│   │   ├── useSSE.ts               # 封装 @microsoft/fetch-event-source
│   │   └── usePagination.ts
│   │
│   ├── lib/                        # 纯工具函数，无副作用
│   │   ├── request.ts              # Axios 实例 + 请求/响应拦截器 + TraceId 注入
│   │   ├── cn.ts                   # clsx + tailwind-merge 的 cn() 工具
│   │   ├── sse.ts                  # SSE 批量 flush 工具（攒 16ms 再 setState）
│   │   └── utils.ts                # 日期格式化、文件大小、防抖等
│   │
│   ├── stores/                     # Zustand 全局状态
│   │   ├── authStore.ts            # 用户信息、accessToken、登录态（persist）
│   │   └── uiStore.ts              # 侧边栏折叠、全局 loading、主题
│   │
│   ├── types/                      # 全局 TypeScript 类型定义
│   │   ├── api.ts                  # Result<T>、PageResult<T>、ErrorResponse
│   │   ├── auth.ts
│   │   ├── kb.ts
│   │   ├── document.ts
│   │   └── chat.ts
│   │
│   └── main.tsx
│
├── tailwind.config.ts
├── vite.config.ts
├── tsconfig.json
└── package.json
```

### 7.2 页面结构（产品视角）

> 共 **4 个一级板块**，侧边栏常驻导航；认证页独立布局，无侧边栏。

```
AI 知识库
│
├── 🔐 认证（AuthLayout — 居中卡片，无侧边栏）
│   ├── /login                登录
│   └── /register             注册
│
├── 📚 知识库  /kb             ← 侧边栏入口①
│   ├── /kb                   知识库列表      卡片式展示所有 KB，支持新建/搜索
│   ├── /kb/:kbId             知识库详情
│   │   ├── Tab: 文档管理     上传文档、查看列表、解析状态轮询
│   │   ├── Tab: 成员管理     邀请成员、设置角色权限
│   │   └── Tab: 基本设置     名称/描述/Embedding 模型/分块策略
│   └── /kb/:kbId/docs/:docId 文档详情（可选）  预览原文 + 分块内容
│
├── 💬 对话  /chat             ← 侧边栏入口②
│   ├── /chat                 对话首页        选择知识库进入对话
│   └── /chat/:conversationId 对话窗口
│       ├── 左栏：历史会话列表（可折叠）
│       └── 右栏：聊天区域（流式输出 + 引用溯源）
│
└── ⚙️ 设置  /settings         ← 侧边栏入口③（或头像下拉）
    ├── /settings/profile     个人资料        修改昵称、头像
    ├── /settings/security    账号安全        修改密码、退出所有设备
    └── /settings/models      模型配置        🔒 管理员可见：配置 LLM / Embedding API Key
```

**侧边栏导航菜单**

| 图标 | 名称 | 路由 | 备注 |
| --- | --- | --- | --- |
| 📚 | 知识库 | `/kb` | 默认首页 |
| 💬 | 对话 | `/chat` | |
| ⚙️ | 设置 | `/settings/profile` | |
| 👤 | 个人中心 | 头像下拉菜单 | 含退出登录 |

**对应 React Router 路由表**

```
/                              → redirect → /kb
├── /login                     → LoginPage       (AuthLayout)
├── /register                  → RegisterPage    (AuthLayout)
└── /                          → AppLayout       (ProtectedRoute)
    ├── /kb                    → KbListPage
    ├── /kb/:kbId              → KbDetailPage    (Tab: 文档/成员/设置)
    ├── /kb/:kbId/docs/:docId  → DocDetailPage
    ├── /chat                  → ChatHomePage
    ├── /chat/:conversationId  → ChatPage
    ├── /settings/profile      → ProfilePage
    ├── /settings/security     → SecurityPage
    └── /settings/models       → ModelConfigPage (管理员)
```

### 7.3 组件分层规则

| 放在哪里 | 判断标准 |
| --- | --- |
| `components/ui/` | shadcn/ui 生成的无状态 UI 原子（Button、Input、Badge…） |
| `components/layout/` | 页面骨架（布局、导航栏），不含业务逻辑 |
| `components/shared/` | 跨 feature 复用的业务通用组件（ConfirmDialog、EmptyState） |
| `features/xxx/components/` | **只在该 feature 内用**的业务组件（DocUploader、CitationCard） |
| `features/xxx/hooks/` | 封装了 TanStack Query 的业务请求 hook |
| `hooks/` | 不含业务的通用 hook（useDebounce、useSSE） |
| `stores/` | 跨页面共享的客户端状态（登录态、UI 状态），**不放服务端数据** |

---

## 八、风险与注意事项

| # | 风险点 | 规避方案 |
| --- | --- | --- |
| 1 | **大模型成本失控** | 开发用 Ollama 本地模型；Bucket4j 限流；Dashboard 监控 Token 消耗；设置用户配额 |
| 2 | **中文 Embedding 效果差** | 优先用 `bge-m3`，显著优于 OpenAI `text-embedding-3-small` |
| 3 | **分块破坏语义** | 先按标点/段落切，再按 token 上限截断（Phase 3 自定义分块） |
| 4 | **LLM 幻觉** | Prompt 强约束"无依据不回答"并强制引用；通过 RAG 评测（Phase 4.5）量化 |
| 5 | **Prompt 注入** | 用户输入用明确分隔符包裹；系统提示约束角色不可被用户输入覆盖 |
| 6 | **Spring AI 版本破坏性变更** | 锁定 1.0.x 正式版，M 版（里程碑）升级需评估 API 兼容性 |
| 7 | **WebFlux / Servlet 混用** | 流式输出统一用 `SseEmitter`（纯 Servlet 容器），不引入 WebFlux starter |
| 8 | **JWT 被盗** | Access Token 短期（15 min）+ Refresh Token 长期（HttpOnly Cookie）；黑名单存 Redis |
| 9 | **Knife4j 被 Security 401 拦截** | 白名单：`/doc.html`、`/v3/api-docs/**`、`/webjars/**` |
| 10 | **PGVector 性能瓶颈** | 数据量 > 100 万时调参 IVFFlat 或改用 HNSW 索引；上线前压测；必要时迁 Milvus |
| 11 | **MinIO 单点故障** | 生产使用分布式模式或替换为阿里云 OSS |
| 12 | **数据合规** | 权限隔离、审计日志；文件上传做 MIME 白名单 + 大小限制 |

---

## 九、下一步

> 建议先完成 **Phase 0**：
> 1. 创建前端工程脚手架
> 2. 编写 `docker-compose.yml` 拉起中间件
> 3. 在 `pom.xml` 中补充 Spring AI、PGVector、MinIO、Tika 依赖
>
> 准备好后告诉我，我可以直接帮你生成对应文件与代码骨架。
