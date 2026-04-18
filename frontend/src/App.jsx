import { useEffect } from 'react'
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useAuthStore } from './stores/authStore'
import { fetchRefresh } from './api/auth'
import PrivateRoute from './components/layout/PrivateRoute'
import LoginPage from './pages/auth/LoginPage'
import HomePage from './pages/home/HomePage'
import ProfilePage from './pages/profile/ProfilePage'

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
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

function AuthInitializer() {
  const { setAuth, clearAuth } = useAuthStore()

  useEffect(() => {
    fetchRefresh()
      .then(({ accessToken, user }) => setAuth(accessToken, user))
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
