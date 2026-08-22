import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import queryClient from '../api/queryClient';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      role: null,
      user: null,
      isAuthenticated: false,
      fcmToken: null,

      login: (token, role, user) => set({
        token,
        role,
        user,
        isAuthenticated: true
      }),

      logout: () => {
        // 1. Reset state
        set({
          token: null,
          role: null,
          user: null,
          isAuthenticated: false,
          fcmToken: null
        });

        // 2. Clear queryClient cache completely so no sensitive role/module data persists
        try {
          queryClient.clear();
        } catch (e) {
          console.warn('Failed to clear queryClient cache on logout:', e);
        }

        // 3. Clear auth storage while preserving user preferences like theme
        try {
          localStorage.removeItem('blood-bridge-auth');
          sessionStorage.clear();
        } catch (e) {
          console.warn('Failed to clear storage on logout:', e);
        }
      },

      setUser: (user) => set({ user }),
      setFcmToken: (fcmToken) => set({ fcmToken }),
    }),
    {
      name: 'blood-bridge-auth',
      onRehydrateStorage: () => (state) => {
        if (state && (!state.token || !state.role)) {
          state.logout();
        }
      }
    }
  )
);

export default useAuthStore;
