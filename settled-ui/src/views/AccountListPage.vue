<script setup lang="ts">
import AccountCreateForm from '@/components/sections/accounts/AccountCreateForm.vue';
import AccountList from '@/components/sections/accounts/AccountList.vue';
import BasePageTitle from '@/components/base/BasePageTitle.vue';
</script>

<template>
  <BasePageTitle>Accounts</BasePageTitle>

  <AccountCreateForm class="mt-4" />
  <AccountList />
</template>
