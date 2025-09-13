const { createGlobPatternsForDependencies } = require('@nx/angular/tailwind');
const { join } = require('path');

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    join(__dirname, 'src/**/!(*.stories|*.spec).{ts,html}'),
    ...createGlobPatternsForDependencies(__dirname),
  ],
  theme: {
    fontFamily: {
      sans: 'Inter var, ui-sans-serif, system-ui',
      serif: 'Inter var, ui-sans-serif, system-ui',
    },
    // fontSize: {
    //   // tailwind default
    //   sm: '0.875rem',
    //   base: '1.3rem',
    //   // lg: '1.125rem',
    //   xl: '1.55rem',
    //   '2xl': '1.563rem',
    //   '3xl': '1.953rem',
    //   '4xl': '2.441rem',
    //   '5xl': '3.052rem',
    // },
    extend: {},
  },
  daisyui: {
    themes: [
      {
        "tageslicht": {
          // #FF8C00 (Dark Orange): This is a vibrant, slightly deeper orange.
          // #FFA500 (Orange): A classic, bright orange.
          // #FF9900 (Web Orange): A common web orange that often appears quite luminous.
          // #FFB700 (Golden Orange): A slightly lighter, more golden orange that can have a glowing effect.
          "primary": "#FF9900", // Dark Orange
          "secondary": "#FFB700", // Light Orange
          "accent": "#3b82f6", // Blue (blue-500)
          "neutral": "#475569", // Medium Slate (slate-600)
          "base-100": "#ffffff", // White
          "base-200": "#f1f5f9", // Light Slate (slate-100)
          "base-300": "#e2e8f0", // Lighter Slate (slate-200)
          "base-content": "#1e293b", // Dark Slate (slate-800)
          "info": "#3abff8", // Cyan (sky-400)
          "success": "#36d399", // Green (emerald-400)
          "warning": "#fbbd23", // Yellow (amber-400)
          "error": "#f87272", // Red (red-400)
        },
      },
      "halloween",
    ],
  },
  plugins: [
    require('@tailwindcss/typography'),
    require('daisyui')
  ],
};
