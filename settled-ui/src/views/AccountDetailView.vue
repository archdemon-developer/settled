<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<template>
  <h2 class="text-xl font-semibold text-slate-900">Account {{ route.params.id }}</h2>
</template>
