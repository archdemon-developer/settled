<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ApiError } from '@/api/client';

import { useLedgerStore } from '@/stores/ledger';
import BaseButton from '@/components/base/BaseButton.vue';
import BaseMessage from '@/components/base/BaseMessage.vue';
import BasePageTitle from '@/components/base/BasePageTitle.vue';

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
    <BaseMessage v-if="isLoading" tone="muted" class="mt-4">Loading account…</BaseMessage>
    <BaseMessage v-else-if="errorMessage && !account" tone="error" class="mt-4">
      {{ errorMessage }}
    </BaseMessage>
    <div v-else-if="account" class="mt-4">
      <BasePageTitle>{{ account.code }}</BasePageTitle>

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

      <BaseMessage v-if="errorMessage" tone="error" class="mt-4">{{ errorMessage }}</BaseMessage>
      <BaseButton
        v-if="account.status === 'ACTIVE'"
        :disabled="isArchiving"
        class="mt-6"
        @click="archive"
      >
        Archive account
      </BaseButton>
    </div>
  </div>
</template>
