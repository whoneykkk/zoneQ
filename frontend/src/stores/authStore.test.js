import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from './authStore'

beforeEach(() => {
  useAuthStore.setState({ accessToken: null, user: null, isInitializing: true })
})

describe('authStore', () => {
  it('sets auth state', () => {
    const user = { id: 1, name: '김철수', email: 'a@b.com', grade: 'S', seat: { zone: 'S', seatNumber: 13 } }
    useAuthStore.getState().setAuth('tok123', user)
    const state = useAuthStore.getState()
    expect(state.accessToken).toBe('tok123')
    expect(state.user).toEqual(user)
    expect(state.isInitializing).toBe(false)
  })

  it('clears auth state', () => {
    useAuthStore.setState({ accessToken: 'tok', user: { name: '테스트' }, isInitializing: false })
    useAuthStore.getState().clearAuth()
    const state = useAuthStore.getState()
    expect(state.accessToken).toBeNull()
    expect(state.user).toBeNull()
    expect(state.isInitializing).toBe(false)
  })

  it('initializes with correct default state', () => {
    useAuthStore.setState(useAuthStore.getInitialState())
    const state = useAuthStore.getState()
    expect(state.isInitializing).toBe(true)
    expect(state.accessToken).toBeNull()
    expect(state.user).toBeNull()
  })

  it('sets isInitializing flag', () => {
    useAuthStore.getState().setInitializing(false)
    expect(useAuthStore.getState().isInitializing).toBe(false)
    useAuthStore.getState().setInitializing(true)
    expect(useAuthStore.getState().isInitializing).toBe(true)
  })
})
