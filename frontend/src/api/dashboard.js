import client from './client'

export const fetchDashboardStats = () =>
  client.get('/dashboard/stats').then((r) => r.data.data)

export const fetchDashboardRealtime = () =>
  client.get('/dashboard/realtime').then((r) => r.data.data)