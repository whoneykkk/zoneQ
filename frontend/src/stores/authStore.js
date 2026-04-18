import { create } from 'zustand'

export const useAuthStore = create((set) => ({
  accessToken: null,
  user: null,
  isInitializing: true,
  setAuth: (token, user) => set({ accessToken: token, user, isInitializing: false }),
  clearAuth: () => set({ accessToken: null, user: null, isInitializing: false }),
  setInitializing: (v) => set({ isInitializing: v }),
}))
