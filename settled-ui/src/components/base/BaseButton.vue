<script setup lang="ts">
withDefaults(defineProps<{ type?: 'button' | 'submit'; disabled?: boolean }>(), {
  type: 'button',
  disabled: false,
});
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    class="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
  >
    <slot />
  </button>
</template>
