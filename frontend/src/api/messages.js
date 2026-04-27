import client from './client'

export const fetchMessagesInbox = () =>
  client.get('/messages/inbox').then((r) => r.data.data)
