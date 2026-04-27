import client from './client'

export const fetchProfileMe = () =>
  client.get('/profile/me').then((r) => r.data.data)