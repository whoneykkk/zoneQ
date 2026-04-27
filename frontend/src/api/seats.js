import client from './client'

export const fetchSeats = (zone) => {
  const params = zone ? { zone } : {}
  return client.get('/seats', { params }).then((r) => r.data.data)
}
