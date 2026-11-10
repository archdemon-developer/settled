<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AccountDetail from '../components/sections/accounts/AccountDetail.vue';

const route = useRoute();
const accountId = computed(() => String(route.params.id));
</script>

<template>
  <div>
    <RouterLink to="/accounts" class="text-sm font-medium text-blue-700 hover:underline">
      Back to accounts
    </RouterLink>
    <AccountDetail :account-id="accountId" />
  </div>
</template>
