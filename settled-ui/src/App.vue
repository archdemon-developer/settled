<script setup lang="ts">
import AppHeader from '@/components/sections/layout/AppHeader.vue';
import AppSidebar from '@/components/sections/layout/AppSidebar.vue';
import AppFooter from '@/components/sections/layout/AppFooter.vue';
</script>
<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader />
    <div class="flex flex-1">
      <AppSidebar />
      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
    <AppFooter />
  </div>
</template>
