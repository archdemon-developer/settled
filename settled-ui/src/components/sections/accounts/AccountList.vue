<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ApiError } from '@/api/client';
import { useLedgerStore } from '@/stores/ledger';

import BaseMessage from '@/components/base/BaseMessage.vue';
const ledger = useLedgerStore();
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);

onMounted(async () => {
  try {
    await ledger.fetchAccounts();
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = `API Error: ${error.message}`;
    } else {
      errorMessage.value = 'An unexpected error occurred.';
    }
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <BaseMessage v-if="isLoading" tone="muted" class="mt-4">Loading accounts...</BaseMessage>
  <BaseMessage v-else-if="errorMessage" tone="error" class="mt-4">{{ errorMessage }}</BaseMessage>
  <BaseMessage v-else-if="ledger.accounts.length === 0" tone="muted" class="mt-4">
    No accounts yet.
  </BaseMessage>

  <table v-else class="mt-4 w-full text-left text-sm">
    <thead class="border-b border-slate-200 text-slate-500">
      <tr>
        <th class="py-2 font-medium">Code</th>
        <th class="py-2 font-medium">Name</th>
        <th class="py-2 font-medium">Type</th>
        <th class="py-2 font-medium">Status</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="account in ledger.accounts" :key="account.id" class="border-b border-slate-100">
        <td class="py-2">
          <RouterLink
            :to="`/accounts/${account.id}`"
            class="font-medium text-blue-700 hover:underline"
          >
            {{ account.code }}
          </RouterLink>
        </td>
        <td class="py-2">{{ account.name }}</td>
        <td class="py-2">{{ account.type }}</td>
        <td class="py-2">{{ account.status }}</td>
      </tr>
    </tbody>
  </table>
</template>
