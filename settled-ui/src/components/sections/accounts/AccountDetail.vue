<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ApiError } from '@/api/client';

import { useLedgerStore } from '@/stores/ledger';

const props = defineProps<{ accountId: string }>();
const ledger = useLedgerStore();

const account = computed(() => ledger.accounts.find((a) => a.id === props.accountId));

const isLoading = ref(true);
const isArchiving = ref(false);
const errorMessage = ref<string | null>();

function toMessage(e: unknown): string {
  return e instanceof ApiError ? e.error.message : 'Could not reach the server';
}

onMounted(async () => {
  try {
    await ledger.fetchAccount(props.accountId);
  } catch (e) {
    errorMessage.value = toMessage(e);
  } finally {
    isLoading.value = false;
  }
});

async function archive() {
  isArchiving.value = true;
  errorMessage.value = null;
  try {
    await ledger.archiveAccount(props.accountId);
  } catch (e) {
    errorMessage.value = toMessage(e);
  } finally {
    isArchiving.value = false;
  }
}
</script>

<template>
  <div>
    <p v-if="isLoading" class="mt-4 text-sm text-slate-500">Loading account…</p>
    <p v-else-if="errorMessage && !account" class="mt-4 text-sm text-red-700">{{ errorMessage }}</p>

    <div v-else-if="account" class="mt-4">
      <h2 class="text-xl font-semibold text-slate-900">{{ account.code }}</h2>

      <dl class="mt-4 grid max-w-md grid-cols-[8rem_1fr] gap-y-2 text-sm">
        <dt class="text-slate-500">Name</dt>
        <dd>{{ account.name }}</dd>
        <dt class="text-slate-500">Type</dt>
        <dd>{{ account.type }}</dd>
        <dt class="text-slate-500">Status</dt>
        <dd>{{ account.status }}</dd>
        <dt class="text-slate-500">Created</dt>
        <dd>{{ new Date(account.createdAt).toLocaleString() }}</dd>
      </dl>

      <p v-if="errorMessage" class="mt-4 text-sm text-red-700">{{ errorMessage }}</p>

      <button
        v-if="account.status === 'ACTIVE'"
        type="button"
        :disabled="isArchiving"
        class="mt-6 rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
        @click="archive"
      >
        Archive account
      </button>
    </div>
  </div>
</template>
