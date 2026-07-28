import React, { useEffect } from 'react';
import { useThemeStore } from '../../store/themeStore';
import { Sun, Moon } from 'lucide-react';

/**
 * Toggle component to switch platform styles between dark mode and light mode.
 */
export default function ThemeToggle() {
  const { theme, toggleTheme, initTheme } = useThemeStore();

  useEffect(() => {
    initTheme();
  }, [initTheme]);

  return (
    <button
      onClick={toggleTheme}
      className="p-2.5 bg-gray-50 border border-gray-100 hover:bg-gray-100 dark:bg-slate-800 dark:border-slate-750 dark:hover:bg-slate-700 text-gray-400 dark:text-slate-200 rounded-xl transition-all outline-none focus-visible:ring-2 focus-visible:ring-primary"
      aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
      title={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
    >
      {theme === 'light' ? (
        <Moon className="h-5 w-5" />
      ) : (
        <Sun className="h-5 w-5" />
      )}
    </button>
  );
}
export { ThemeToggle };
