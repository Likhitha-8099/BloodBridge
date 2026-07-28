import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      role: null,
      user: null,
      isAuthenticated: false,

      login: (token, role, user) => set({
        token,
        role,
        user,
        isAuthenticated: true
      }),

      logout: () => set({
        token: null,
        role: null,
        user: null,
        isAuthenticated: false
      }),

      setUser: (user) => set({ user }),
    }),
    {
      name: 'blood-bridge-auth',
    }
  )
);
