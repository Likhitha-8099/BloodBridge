import { create } from 'zustand';
import { persist } from 'zustand/middleware';

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

      logout: () => set({
        token: null,
        role: null,
        user: null,
        isAuthenticated: false,
        fcmToken: null
      }),

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
