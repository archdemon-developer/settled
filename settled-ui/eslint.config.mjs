import { globalIgnores } from 'eslint/config';
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript';
import pluginVue from 'eslint-plugin-vue';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';

const banParentRelative = {
  group: ['../*'],
  message: 'Use the @/ alias for imports outside the current folder.',
};

function restrictImports(name, files, ...groups) {
  return {
    name: `boundaries/${name}`,
    files,
    rules: {
      'no-restricted-imports': ['error', { patterns: [banParentRelative, ...groups] }],
    },
  };
}

export default defineConfigWithVueTs(
  { name: 'app/files-to-lint', files: ['**/*.{ts,mts,tsx,vue}'] },
  globalIgnores(['**/dist/**', '**/coverage/**']),
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  skipFormatting,

  restrictImports('src', ['src/**/*.{ts,vue}']),
  restrictImports('base', ['src/components/base/**'], {
    group: [
      '@/components/composite/**',
      '@/components/sections/**',
      '@/views/**',
      '@/stores/**',
      '@/api/**',
    ],
    message: 'base components are domain-free: no higher levels, stores or API.',
  }),
  restrictImports('composite', ['src/components/composite/**'], {
    group: ['@/components/sections/**', '@/views/**', '@/stores/**', '@/api/**'],
    message: 'composite components are domain-free: no sections, views, stores or API.',
  }),
  restrictImports('sections', ['src/components/sections/**'], {
    group: ['@/views/**'],
    message: 'sections must not import views.',
  }),
  restrictImports('views', ['src/views/**'], {
    group: ['@/stores/**', '@/api/**'],
    message: 'views stay thin: sections own data fetching.',
  }),
  restrictImports('composables-utils', ['src/composables/**', 'src/utils/**'], {
    group: ['@/components/**', '@/views/**'],
    message: 'composables and utils must not import components or views.',
  }),
);
