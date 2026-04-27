import client from './client'

export const fetchNotificationsMe = () =>
  client.get('/notifications/me').then((r) => r.data.data)

export const markNotificationRead = (id) =>
  client.patch(`/notifications/${id}/read`).then((r) => r.data)
