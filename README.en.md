# AI Knowledge Base Platform

An intelligent knowledge base management platform based on RAG (Retrieval-Augmented Generation) technology, supporting document upload, intelligent Q&A, and conversation management.

## Project Overview

The AI Knowledge Base Platform is a complete enterprise-grade application designed to help teams build and manage private knowledge bases, combining large language models for intelligent question answering. The system adopts a frontend-backend separation architecture: the backend is built with Spring Boot, and the frontend uses React + TypeScript + Vite.

## Technology Stack

### Backend

- **Framework**: Spring Boot 3.x
- **Database**: MySQL + PostgreSQL (pgvector for vector storage)
- **Cache**: Redis
- **AI Capabilities**: Spring AI + OpenAI API
- **Security**: Spring Security + JWT
- **ORM**: MyBatis Plus

### Frontend

- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS + shadcn/ui
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Routing**: React Router

## Core Features

### Knowledge Base Management
- Create, edit, and delete knowledge bases
- Member permission management (Owner/Editor/Viewer)
- Knowledge base analytics and statistics

### Document Management
- Upload documents in multiple formats (PDF, Word, txt, etc.)
- Asynchronous document parsing
- Intelligent text chunking and vectorization
- Re-parse and download documents

### Intelligent Q&A (RAG)
- Vector-based semantic retrieval
- HyDE enhanced retrieval
- Rerank optimization
- SSE streaming responses
- Citation溯源 (citation tracing)

### Conversation Management
- Multi-session support
- Historical message logging
- Session bookmarking and export

### System Management
- Multi-model configuration
- User authentication and authorization

## Project Structure

```
ai-knowledge-base/
├── ai-knowledge-base-backend/     # Spring Boot backend
│   └── src/main/java/com/smartdocs/aikb/
│       ├── common/               # Common components (exceptions, results, utilities)
│       ├── config/               # Configuration classes
│       ├── module/               # Business modules
│       │   ├── chat/             # Chat module
│       │   ├── kb/               # Knowledge base module
│       │   ├── system/           # System module
│       │   └── user/             # User module
│       └── security/             # Security components
│
├── ai-knowledge-base-frontend/   # React frontend
│   └── src/
│       ├── app/                  # Application entry
│       ├── components/           # Shared components
│       │   ├── layout/           # Layout components
│       │   ├── shared/           # Shared components
│       │   └── ui/               # UI components
│       ├── features/              # Feature modules
│       │   ├── auth/              # Authentication
│       │   ├── chat/              # Chat
│       │   ├── document/          # Document
│       │   ├── knowledge-base/    # Knowledge Base
│       │   └── settings/          # Settings
│       ├── lib/                   # Utility libraries
│       ├── stores/                # State management
│       └── types/                  # Type definitions
│
└── deploy/                        # Deployment configuration
```

## Quick Start

### Backend Startup

1. Ensure JDK 17+, MySQL, PostgreSQL, and Redis are installed.

2. Configure `application-dev.yml`:

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

3. Start the application:

```bash
cd ai-knowledge-base-backend
mvn spring-boot:run
```

### Frontend Startup

1. Install dependencies:

```bash
cd ai-knowledge-base-frontend
pnpm install
```

2. Start the development server:

```bash
pnpm dev
```

3. Access http://localhost:5173

### Docker Deployment

```bash
cd deploy
docker-compose up -d
```

## API Overview

| Module | Endpoint | Description |
|--------|----------|-------------|
| Authentication | POST /auth/register | User registration |
| Authentication | POST /auth/login | User login |
| Knowledge Base | GET/POST /kb | Retrieve/create knowledge base |
| Document | POST /kb/{kbId}/documents | Upload document |
| Q&A | POST /chat/stream | Streaming Q&A |
| Conversations | GET /chat/conversations | Retrieve conversation list |

## Development Guide

For detailed development procedures, refer to:

- [Backend Technology Stack Learning Roadmap](./LEARNING_ROADMAP.md)
- [Frontend Technology Stack Learning Roadmap](./FRONTEND_LEARNING_ROADMAP.md)
- [RAG Flow Detailed Guide](./RAG_FLOW_GUIDE.md)

## License

MIT License