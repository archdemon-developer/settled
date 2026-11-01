<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ApiError } from '../api/client';
import { useLedgerStore } from '../stores/ledger';
import AccountCreateForm from '../components/AccountCreateForm.vue';

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
  <h2 class="text-xl font-semibold text-slate-900">Accounts</h2>

  <AccountCreateForm class="mt-4" />

  <p v-if="isLoading" class="mt-4 text-sm text-slate-500">Loading accounts...</p>
  <p v-else-if="errorMessage" class="mt-4 text-sm text-red-700">{{ errorMessage }}</p>
  <p v-else-if="ledger.accounts.length === 0" class="mt-4 text-sm text-slate-500">
    No accounts yet.
  </p>

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
