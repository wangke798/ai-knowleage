import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { AppLayout } from '@/components/layout/AppLayout'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'

import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { KbListPage } from '@/features/knowledge-base/pages/KbListPage'
import { KbDetailPage } from '@/features/knowledge-base/pages/KbDetailPage'
import { ChatHomePage } from '@/features/chat/pages/ChatHomePage'
import { ChatPage } from '@/features/chat/pages/ChatPage'
import { ProfilePage } from '@/features/settings/pages/ProfilePage'
import { SecurityPage } from '@/features/settings/pages/SecurityPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/kb" replace />,
  },
  {
    element: <AuthLayout />,
    children: [
      { path: 'login',    element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
    ],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'kb',                            element: <KbListPage /> },
          { path: 'kb/:kbId',                      element: <KbDetailPage /> },
          { path: 'chat',                          element: <ChatHomePage /> },
          { path: 'chat/:conversationId',          element: <ChatPage /> },
          { path: 'settings/profile',              element: <ProfilePage /> },
          { path: 'settings/security',             element: <SecurityPage /> },
        ],
      },
    ],
  },
])
