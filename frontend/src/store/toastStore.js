import { create } from 'zustand';

/**
 * Global store managing notifications toasts, supporting type states (success, error, warning, info).
 */
export const useToastStore = create((set) => ({
  toasts: [],
  /**
   * Adds a new toast and sets a 4-second expiration timer.
   */
  addToast: (message, type = 'info') => {
    const id = Date.now();
    set((state) => ({
      toasts: [...state.toasts, { id, message, type }],
    }));
    setTimeout(() => {
      set((state) => ({
        toasts: state.toasts.filter((t) => t.id !== id),
      }));
    }, 4000);
  },
  /**
   * Manually removes a toast by ID.
   */
  removeToast: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((t) => t.id !== id),
    })),
}));

export default useToastStore;
