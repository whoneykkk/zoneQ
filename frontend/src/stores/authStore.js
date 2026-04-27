import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isInitializing: true,
  setAuth: (token, user, refresh = null) => set({ accessToken: token, user, isInitializing: false, refreshToken: refresh }),
  setUser: (user) => set({ user }),
  clearAuth: () => set({ accessToken: null, refreshToken: null, user: null, isInitializing: false }),
  setInitializing: (v) => set({ isInitializing: v }),
}))
