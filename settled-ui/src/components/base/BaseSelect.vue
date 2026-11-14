<script setup lang="ts" generic="T extends string">
const model = defineModel<T>({ required: true });

defineProps<{ options: { value: T; label: string }[] }>();
</script>

<template>
  <select v-model="model" class="rounded border border-slate-300 px-3 py-2 text-sm">
    <option v-for="option in options" :key="option.value" :value="option.value">
      {{ option.label }}
    </option>
  </select>
</template>
