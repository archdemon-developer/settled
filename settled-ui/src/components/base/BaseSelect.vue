<script setup lang="ts" generic="T extends string">
const model = defineModel<T>({ required: true });
</script>

<template>
  <select v-model="model" class="rounded border border-slate-300 px-3 py-2 text-sm">
    <slot />
  </select>
</template>
