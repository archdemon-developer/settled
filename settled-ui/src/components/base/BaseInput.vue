<script setup lang="ts">
const model = defineModel<string>({ required: true });
</script>

<template>
  <input v-model="model" class="rounded border border-slate-300 px-3 py-2 text-sm" />
</template>
