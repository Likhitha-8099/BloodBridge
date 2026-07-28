/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#DC2626',
          dark: '#991B1B',
        },
        secondary: '#991B1B',
        success: '#16A34A',
        background: '#F8FAFC',
      }
    },
  },
  plugins: [],
}
