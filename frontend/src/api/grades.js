import client from './client'

export const fetchGradesHistoryMe = () =>
  client.get('/grades/history/me').then((r) => r.data.data)

export const fetchGradeMe = () =>
  client.get('/grades/me').then((r) => r.data.data)