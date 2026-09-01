/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  // Disable preflight — Angular Material handles its own base styles
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#e3f2fd',
          100: '#bbdefb',
          200: '#90caf9',
          300: '#64b5f6',
          400: '#42a5f5',
          500: '#1976d2',
          600: '#1565c0',
          700: '#0d47a1',
          800: '#0a3d8f',
          900: '#072b6b',
        },
      },
    },
  },
  plugins: [],
}
