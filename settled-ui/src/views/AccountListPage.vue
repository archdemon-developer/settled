<script setup lang="ts">
import AccountCreateForm from '../components/sections/accounts/AccountCreateForm.vue';
import AccountList from '../components/sections/accounts/AccountList.vue';
</script>

<template>
  <h2 class="text-xl font-semibold text-slate-900">Accounts</h2>

  <AccountCreateForm class="mt-4" />
  <AccountList />
</template>
