import { create } from 'zustand';

/**
 * Global store managing dark/light themes, toggles, and localStorage state persistence.
 */
export const useThemeStore = create((set) => ({
  theme: localStorage.getItem('theme') || 'light',
  /**
   * Toggles theme state between 'light' and 'dark', writing target classes to html tag elements.
   */
  toggleTheme: () => set((state) => {
    const next = state.theme === 'light' ? 'dark' : 'light';
    localStorage.setItem('theme', next);
    if (next === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    return { theme: next };
  }),
  /**
   * Initializes theme during mount render states.
   */
  initTheme: () => {
    const theme = localStorage.getItem('theme') || 'light';
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}));

export default useThemeStore;
