import client from './client'

export const fetchNotices = () =>
  client.get('/notices', { params: { page: 0, size: 20 } }).then((r) => r.data.data)

export const fetchNotice = (id) =>
  client.get(`/notices/${id}`).then((r) => r.data.data)