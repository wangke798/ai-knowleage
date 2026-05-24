# 前端技术栈学习路线

## 技术全景

```
React 18 + TypeScript（核心基础）
├── Vite 8                         构建工具与开发服务器
├── React Router v7                客户端路由（SPA）
├── TanStack Query v5              服务端状态管理（数据请求 / 缓存）
├── Zustand v5                     客户端全局状态（Auth / UI）
├── React Hook Form + Zod          表单处理与校验
├── Tailwind CSS v3                原子化 CSS 样式
├── shadcn/ui + Radix UI           无样式 UI 组件库
├── Axios                          HTTP 请求封装
├── @microsoft/fetch-event-source  SSE 流式输出（AI 打字机）
├── @tanstack/react-virtual         虚拟滚动（长列表性能）
├── react-markdown + rehype        Markdown 渲染 + 代码高亮
├── react-pdf + pdfjs-dist         PDF 文件预览
├── date-fns                       日期格式化
├── lucide-react                   图标库
└── ESLint + Prettier              代码规范
```

---

## 项目结构总览

```
src/
├── app/
│   ├── router.tsx          路由配置（所有页面入口）
│   └── App.tsx             根组件
├── components/
│   ├── layout/             布局组件（AppLayout / AuthLayout / Sidebar）
│   ├── shared/             通用组件（EmptyState / ConfirmDialog）
│   └── ui/                 shadcn/ui 基础组件（Button / Input / Card...）
├── features/               按业务模块划分
│   ├── auth/               登录 / 注册
│   ├── chat/               AI 对话（流式输出核心）
│   ├── document/           文档管理与 PDF 预览
│   ├── knowledge-base/     知识库（统计 / 检索 / 成员）
│   └── settings/           个人设置 / 模型配置
├── lib/
│   ├── request.ts          Axios 封装（Token 注入 / 错误处理）
│   ├── sse.ts              SSE 客户端（AI 流式通信）
│   └── utils.ts            工具函数（cn / 格式化）
├── stores/
│   ├── authStore.ts        登录状态持久化（Zustand）
│   └── uiStore.ts          UI 状态（侧边栏折叠等）
└── types/                  全局 TypeScript 类型定义
```

---

## 第一阶段：语言与工具基础（第 1-2 周）

**目标**：熟悉项目所用语言和工程工具，能独立运行项目

| 优先级 | 技术 | 对应代码 | 学习要点 |
|---|---|---|---|
| ⭐⭐⭐ | **TypeScript** | 所有 `.ts` / `.tsx` 文件 | 类型注解、接口 `interface`、泛型 `<T>`、联合类型、类型断言 |
| ⭐⭐⭐ | **Vite** | `vite.config.ts`、`package.json` | 开发服务器、路径别名 `@/`、`pnpm dev` / `pnpm build` |
| ⭐⭐ | **ESLint + Prettier** | `eslint.config.js` | 代码风格约束、自动格式化 |
| ⭐⭐ | **pnpm** | `pnpm-workspace.yaml`、`pnpm-lock.yaml` | 包管理、工作区、`pnpm add` |

**建议阅读顺序**：

1. `vite.config.ts` → 了解路径别名 `@/` 配置
2. `tsconfig.app.json` → 理解严格模式与路径映射
3. `src/types/` → 浏览全局类型定义，了解数据结构
4. `package.json` → 熟悉所有依赖和 script 命令

---

## 第二阶段：React 核心与路由（第 3-4 周）

**目标**：能读懂所有页面组件，理解页面跳转与权限控制

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **React 18 基础** | `src/features/auth/pages/LoginPage.tsx` | `useState`、`useEffect`、`useRef`、组件拆分 |
| **React Router v7** | `src/app/router.tsx`、`components/layout/ProtectedRoute.tsx` | `createBrowserRouter`、嵌套路由、`<Navigate>`、`useParams`、`useNavigate` |
| **组件设计模式** | `src/components/layout/`、`src/components/shared/` | Layout 组件、插槽 `children`、受控 / 非受控组件 |
| **TypeScript + React** | 所有 `.tsx` 文件 | `FC`、Props 类型、`React.forwardRef`、事件类型 |

**建议阅读顺序**：

1. `src/app/router.tsx` → 了解所有路由和布局嵌套结构
2. `components/layout/ProtectedRoute.tsx` → 路由权限守卫实现
3. `components/layout/AppLayout.tsx` → 主布局组件结构
4. `features/auth/pages/LoginPage.tsx` → 最基础的页面组件

---

## 第三阶段：状态管理与数据请求（第 5-6 周）

**目标**：理解数据如何在前端流动，掌握 React Query 的核心用法

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **TanStack Query v5** | `features/*/hooks/use*.ts`、所有 `api.ts` | `useQuery`、`useMutation`、`queryKey`、`invalidateQueries`、自动缓存与重请求 |
| **Axios 封装** | `src/lib/request.ts` | 拦截器、Token 注入、统一错误处理、`Result<T>` 响应解析 |
| **Zustand v5** | `stores/authStore.ts`、`stores/uiStore.ts` | `create`、持久化中间件 `persist`、跨组件共享状态 |
| **React Hook Form + Zod** | `features/auth/pages/RegisterPage.tsx`、各 Dialog 组件 | `useForm`、`register`、`handleSubmit`、Zod Schema 校验 |

**建议阅读顺序**：

1. `stores/authStore.ts` → 登录状态如何持久化到 localStorage
2. `lib/request.ts` → Axios 拦截器如何自动带上 JWT Token
3. `features/auth/hooks/useAuth.ts` → 登录 / 注册 Mutation 封装
4. `features/knowledge-base/hooks/useKnowledgeBases.ts` → Query + Mutation 的完整示例
5. `features/knowledge-base/api.ts` → API 层封装规范

---

## 第四阶段：UI 样式体系（第 7 周）

**目标**：能独立开发符合项目风格的 UI 页面

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **Tailwind CSS v3** | 所有 JSX 中的 `className` | 原子类、响应式前缀、暗色模式 `dark:`、`@apply` 指令 |
| **shadcn/ui** | `src/components/ui/` | 组件复制模式、`cn()` 工具函数、`class-variance-authority` 变体 |
| **Radix UI** | `components/ui/dialog.tsx`、`components/ui/tabs.tsx` | 无障碍、受控 / 非受控、Radix 原语组件 |
| **lucide-react** | 所有组件中的图标引用 | SVG 图标按需引入 |

**建议阅读顺序**：

1. `src/lib/utils.ts` → `cn()` 函数如何合并 Tailwind 类
2. `components/ui/button.tsx` → `cva` 变体系统示例
3. `components/ui/dialog.tsx` → Radix UI Primitive 封装模式
4. `tailwind.config.js` → 主题颜色、字体等自定义配置

---

## 第五阶段：AI 功能与高级特性（第 8-10 周）⭐ 重点

**目标**：掌握本项目最有特色的 AI 对话流式输出与性能优化

### 5.1 SSE 流式输出（打字机效果）

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **fetch-event-source** | `src/lib/sse.ts` | POST + SSE、`Authorization` 头、按事件名分发 |
| **流式状态管理** | `features/chat/hooks/useChatStream.ts` | 边接收边更新 state、`AbortController` 取消请求 |
| **Markdown 渲染** | `features/chat/components/MessageBubble.tsx` | `react-markdown`、`rehype-highlight` 代码高亮、`rehype-katex` 数学公式 |

```
后端 SSE 事件流
    │  event: token  data: "你好"
    │  event: token  data: "，"
    │  event: citations  data: [{...}]
    │  event: done
    ▼
sse.ts → 按 event name 分发
    ▼
useChatStream.ts → 追加 token 到消息 state
    ▼
MessageBubble.tsx → react-markdown 实时渲染
```

### 5.2 虚拟滚动（长列表性能）

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **@tanstack/react-virtual** | `features/chat/components/ConversationList.tsx` | `useVirtualizer`、`estimateSize`、只渲染可见行 |

### 5.3 PDF 预览

| 技术 | 对应代码 | 学习要点 |
|---|---|---|
| **react-pdf + pdfjs-dist** | `features/document/pages/DocDetailPage.tsx` | Worker 配置、`<Document>` + `<Page>` 组件、懒加载分页 |

**建议阅读顺序**：

1. `lib/sse.ts` → SSE 客户端封装，理解事件分发机制
2. `features/chat/hooks/useChatStream.ts` → 流式 token 累积逻辑
3. `features/chat/pages/ChatPage.tsx` → 完整对话页面组装
4. `features/chat/components/MessageBubble.tsx` → Markdown + 代码高亮渲染
5. `features/chat/components/ConversationList.tsx` → 虚拟滚动实现

---

## 第六阶段：工程规范与部署（第 11 周）

| 技术 | 学习要点 |
|---|---|
| **Feature-Sliced 目录规范** | `features/` 按业务模块划分，每模块含 `api/hooks/pages/components` 四层 |
| **TypeScript 严格模式** | `tsconfig.app.json` 中 `strict: true`，消除隐式 any |
| **Vite 生产构建** | `pnpm build`、`tsc -b` 类型检查、代码分割与懒加载 |
| **环境变量** | `.env` 文件、`import.meta.env.VITE_*` 访问方式 |

---

## 总体时间规划

| 周次 | 内容 | 预计用时 |
|---|---|---|
| 第 1-2 周 | TypeScript + Vite + pnpm 工具链 | 2 周 |
| 第 3-4 周 | React 18 + React Router v7 + 组件设计 | 2 周 |
| 第 5-6 周 | TanStack Query + Axios + Zustand + React Hook Form | 2 周 |
| 第 7 周 | Tailwind CSS + shadcn/ui + Radix UI | 1 周 |
| 第 8-10 周 | SSE 流式输出 + 虚拟滚动 + PDF 预览 + Markdown 渲染 | 3 周 |
| 第 11 周 | 工程规范 + 生产构建 + 环境变量 | 1 周 |

> **总计约 11 周**，每天 1.5 ~ 2 小时。
> 第 8-10 周的 SSE 流式 AI 对话是本项目前端最核心的技术亮点，建议重点投入。

---

## 推荐参考资料

| 技术 | 推荐资料 |
|---|---|
| TypeScript | [官方手册](https://www.typescriptlang.org/docs/handbook/intro.html) |
| React 18 | [官方文档（beta）](https://react.dev/) |
| React Router v7 | [官方文档](https://reactrouter.com/home) |
| TanStack Query v5 | [官方文档](https://tanstack.com/query/latest/docs/framework/react/overview) |
| Zustand v5 | [官方文档](https://zustand.docs.pmnd.rs/) |
| Tailwind CSS | [官方文档](https://tailwindcss.com/docs) |
| shadcn/ui | [官方文档](https://ui.shadcn.com/) |
| Radix UI | [官方文档](https://www.radix-ui.com/) |
| Vite | [官方文档](https://vite.dev/) |
| React Hook Form | [官方文档](https://react-hook-form.com/) |
| Zod | [官方文档](https://zod.dev/) |
| TanStack Virtual | [官方文档](https://tanstack.com/virtual/latest) |
