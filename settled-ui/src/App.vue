<script setup lang="ts">
import AppHeader from './components/layout/AppHeader.vue';
import AppSidebar from './components/layout/AppSidebar.vue';
import AppFooter from './components/layout/AppFooter.vue';
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader />
    <div class="flex flex-1">
      <AppSidebar />
      <main class="flex-1 p-6"></main>
    </div>
    <AppFooter />
  </div>
</template>
