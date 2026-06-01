import { Outlet } from 'react-router-dom'
import { FileStack } from 'lucide-react'

export function AuthLayout() {
  return (
    <div className="min-h-screen flex w-full">
      {/* 左侧品牌/介绍展示区（仅大屏可见） */}
      <div className="hidden lg:flex w-1/2 flex-col justify-between bg-zinc-900 p-12 text-zinc-100">
        <div className="flex items-center gap-2 font-semibold text-lg">
          <FileStack className="h-6 w-6" />
          <span>SmartDocs AI</span>
        </div>
        
        <div className="space-y-4">
          <h1 className="text-4xl font-bold tracking-tight">
            构建您的企业级 AI 知识库
          </h1>
          <p className="text-zinc-400 text-lg">
            基于 RAG 技术，上传文档即可快速生成专属 AI 助手。支持多种文档格式，极速解析检索，提供精准回答。
          </p>
        </div>

        <div className="text-sm text-zinc-500">
          © {new Date().getFullYear()} SmartDocs AI. All rights reserved.
        </div>
      </div>

      {/* 右侧表单区 */}
      <div className="flex w-full lg:w-1/2 items-center justify-center bg-background p-6">
        <div className="w-full max-w-[400px]">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
