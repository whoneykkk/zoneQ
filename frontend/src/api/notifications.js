import client from './client'

export const fetchNotificationsMe = () =>
  client.get('/notifications/me').then((r) => r.data.data)
