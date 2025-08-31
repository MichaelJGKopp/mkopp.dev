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
    themes: ['light', 'dark'], // optional: use DaisyUI's built-in themes
  },
  plugins: [
    require('@tailwindcss/typography'),
    require('daisyui')
  ],
};
