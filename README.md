

# AI 知识库平台

基于 RAG（检索增强生成）技术的智能知识库管理平台，支持文档上传、智能问答与对话管理。

## 项目简介

AI 知识库平台是一个完整的企业级应用，旨在帮助团队构建和管理私有知识库，结合大语言模型实现智能问答。系统采用前后端分离架构，后端基于 Spring Boot 构建，前端使用 React + TypeScript + Vite。

## 技术栈

### 后端

- **框架**: Spring Boot 3.x
- **数据库**: MySQL + PostgreSQL (pgvector 向量存储)
- **缓存**: Redis
- **AI 能力**: Spring AI + OpenAI API
- **安全**: Spring Security + JWT
- **ORM**: MyBatis Plus

### 前端

- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **样式**: Tailwind CSS + shadcn/ui
- **状态管理**: Zustand
- **HTTP 客户端**: Axios
- **路由**: React Router

## 核心功能

### 知识库管理
- 创建、编辑、删除知识库
- 知识库成员权限管理（所有者/编辑者/查看者）
- 知识库统计分析

### 文档管理
- 多格式文档上传（支持 PDF、Word、txt 等）
- 异步文档解析
- 文本智能分块与向量化
- 文档重新解析与下载

### 智能问答（RAG）
- 基于向量的语义检索
- HyDE 增强检索
- Rerank 重排优化
- SSE 流式响应
- 引用溯源

### 会话管理
- 多会话支持
- 历史消息记录
- 会话收藏与导出

### 系统管理
- 多模型配置
- 用户认证与授权

## 项目结构

```
ai-knowledge-base/
├── ai-knowledge-base-backend/     # Spring Boot 后端
│   └── src/main/java/com/smartdocs/aikb/
│       ├── common/               # 通用组件（异常、结果、工具类）
│       ├── config/               # 配置类
│       ├── module/               # 业务模块
│       │   ├── chat/             # 聊天模块
│       │   ├── kb/               # 知识库模块
│       │   ├── system/           # 系统模块
│       │   └── user/             # 用户模块
│       └── security/             # 安全组件
│
├── ai-knowledge-base-frontend/   # React 前端
│   └── src/
│       ├── app/                  # 应用入口
│       ├── components/           # 公共组件
│       │   ├── layout/           # 布局组件
│       │   ├── shared/           # 共享组件
│       │   └── ui/               # UI 组件
│       ├── features/              # 功能模块
│       │   ├── auth/              # 认证
│       │   ├── chat/              # 聊天
│       │   ├── document/          # 文档
│       │   ├── knowledge-base/    # 知识库
│       │   └── settings/          # 设置
│       ├── lib/                   # 工具库
│       ├── stores/                # 状态管理
│       └── types/                  # 类型定义
│
└── deploy/                        # 部署配置
```

## 快速开始

### 后端启动

1. 确保已安装 JDK 17+、MySQL、PostgreSQL、Redis

2. 配置 `application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aikb
    url-pgvector: jdbc:postgresql://localhost:5432/aikb_vector
  redis:
    host: localhost
    
app:
  jwt:
    secret: your-secret-key
```

3. 启动应用:

```bash
cd ai-knowledge-base-backend
mvn spring-boot:run
```

### 前端启动

1. 安装依赖:

```bash
cd ai-knowledge-base-frontend
pnpm install
```

2. 启动开发服务器:

```bash
pnpm dev
```

3. 访问 http://localhost:5173

### Docker 部署

```bash
cd deploy
docker-compose up -d
```

## API 概览

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /auth/register | 用户注册 |
| 认证 | POST /auth/login | 用户登录 |
| 知识库 | GET/POST /kb | 获取/创建知识库 |
| 文档 | POST /kb/{kbId}/documents | 上传文档 |
| 问答 | POST /chat/stream | 流式问答 |
| 会话 | GET /chat/conversations | 获取会话列表 |

## 开发指南

详细开发流程请参考:

- [后端技术栈学习路线](./LEARNING_ROADMAP.md)
- [前端技术栈学习路线](./FRONTEND_LEARNING_ROADMAP.md)
- [RAG 流程详解](./RAG_FLOW_GUIDE.md)

## 许可证

MIT License