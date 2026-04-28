import { useEffect } from 'react'
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useAuthStore } from './stores/authStore'
import { fetchRefresh } from './api/auth'
import { fetchProfileMe } from './api/profile'
import PrivateRoute from './components/layout/PrivateRoute'
import LoginPage from './pages/auth/LoginPage'
import HomePage from './pages/home/HomePage'
import ProfilePage from './pages/profile/ProfilePage'
import MessagesPage from './pages/messages/MessagesPage'
import MessageDetailPage from './pages/messages/MessageDetailPage'
import ComposePage from './pages/messages/ComposePage'
import NoticesPage from './pages/notices/NoticesPage'
import NoticeDetailPage from './pages/notices/NoticeDetailPage'
import NotificationsPage from './pages/notifications/NotificationsPage'
import DashboardPage from './pages/dashboard/DashboardPage'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
})

const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <PrivateRoute />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/profile', element: <ProfilePage /> },
      { path: '/messages', element: <MessagesPage /> },
      { path: '/messages/:id', element: <MessageDetailPage /> },
      { path: '/messages/compose', element: <ComposePage /> },
      { path: '/notices', element: <NoticesPage /> },
      { path: '/notices/:id', element: <NoticeDetailPage /> },
      { path: '/notifications', element: <NotificationsPage /> },
    ],
  },
  {
    element: <PrivateRoute adminOnly />,
    children: [
      { path: '/dashboard', element: <DashboardPage /> },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

function AuthInitializer() {
  const { refreshToken, setAuth, setUser, clearAuth } = useAuthStore()

  useEffect(() => {
    if (!refreshToken) { clearAuth(); return }
    fetchRefresh(refreshToken)
      .then(({ accessToken, refreshToken: newRefresh }) => {
        setAuth(accessToken, null, newRefresh)
        return fetchProfileMe()
      })
      .then((profile) => setUser(profile))
      .catch(() => clearAuth())
  }, [])

  return null
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthInitializer />
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}
