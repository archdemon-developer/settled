<script setup lang="ts">
import BaseMessage from '@/components/base/BaseMessage.vue';

defineProps<{ id: string; label: string; error?: string }>();
defineSlots<{ default(props: { id: string }): unknown }>();
</script>

<template>
  <div class="flex flex-col gap-1">
    <label :for="id" class="text-sm font-medium text-slate-900">{{ label }}</label>
    <slot :id="id" />
    <BaseMessage v-if="error" tone="error">{{ error }}</BaseMessage>
  </div>
</template>
