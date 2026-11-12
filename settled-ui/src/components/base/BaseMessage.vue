<script setup lang="ts">
defineProps<{ tone: 'error' | 'muted' }>();

const toneClasses = { error: 'text-red-700', muted: 'text-slate-500' };
</script>

<template>
  <p class="text-sm" :class="toneClasses[tone]">
    <slot />
  </p>
</template>
