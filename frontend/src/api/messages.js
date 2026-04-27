import client from './client'

export const fetchMessagesInbox = () =>
  client.get('/messages/inbox').then((r) => r.data.data)

export const fetchMessage = (id) =>
  client.get(`/messages/${id}`).then((r) => r.data.data)

export const createMessage = (data) =>
  client.post('/messages', data).then((r) => r.data.data)

export const replyMessage = (id, data) =>
  client.post(`/messages/${id}/reply`, data).then((r) => r.data.data)
