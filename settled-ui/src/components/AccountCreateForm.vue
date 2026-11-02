<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ApiError } from '../api/client';
import { useLedgerStore } from '../stores/ledger';
import { ACCOUNT_TYPES } from '../types/api';
import type { AccountType } from '../types/api';

const ledger = useLedgerStore();

const form = reactive({ code: '', name: '', type: 'CHECKING' as AccountType });
const fieldErrors = ref<Record<string, string>>({});
const formError = ref<string | null>(null);
const isSubmitting = ref(false);

async function submit() {
  fieldErrors.value = {};
  formError.value = null;
  isSubmitting.value = true;
  try {
    await ledger.createAccount({ ...form });
    form.code = '';
    form.name = '';
  } catch (e) {
    if (e instanceof ApiError && e.error.violations?.length) {
      fieldErrors.value = Object.fromEntries(e.error.violations.map((v) => [v.field, v.message]));
    } else if (e instanceof ApiError && e.error.field) {
      fieldErrors.value = { [e.error.field]: e.error.message };
    } else if (e instanceof ApiError) {
      formError.value = e.error.message;
    } else {
      formError.value = 'Could not reach the server';
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <form class="flex max-w-md flex-col gap-4" @submit.prevent="submit">
    <div class="flex flex-col gap-1">
      <label for="account-code" class="text-sm font-medium text-slate-900">Code</label>
      <input
        id="account-code"
        v-model="form.code"
        required
        maxlength="50"
        class="rounded border border-slate-300 px-3 py-2 text-sm"
      />
      <p v-if="fieldErrors.code" class="text-sm text-red-700">{{ fieldErrors.code }}</p>
    </div>

    <div class="flex flex-col gap-1">
      <label for="account-name" class="text-sm font-medium text-slate-900">Name</label>
      <input
        id="account-name"
        v-model="form.name"
        required
        maxlength="255"
        class="rounded border border-slate-300 px-3 py-2 text-sm"
      />
      <p v-if="fieldErrors.name" class="text-sm text-red-700">{{ fieldErrors.name }}</p>
    </div>

    <div class="flex flex-col gap-1">
      <label for="account-type" class="text-sm font-medium text-slate-900">Type</label>
      <select
        id="account-type"
        v-model="form.type"
        class="rounded border border-slate-300 px-3 py-2 text-sm"
      >
        <option v-for="type in ACCOUNT_TYPES" :key="type" :value="type">{{ type }}</option>
      </select>
      <p v-if="fieldErrors.type" class="text-sm text-red-700">{{ fieldErrors.type }}</p>
    </div>

    <p v-if="formError" class="text-sm text-red-700">{{ formError }}</p>

    <button
      type="submit"
      :disabled="isSubmitting"
      class="self-start rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
    >
      Create account
    </button>
  </form>
</template>
