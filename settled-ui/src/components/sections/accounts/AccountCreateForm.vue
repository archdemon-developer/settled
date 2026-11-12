<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ApiError } from '@/api/client';
import { useLedgerStore } from '@/stores/ledger';
import { ACCOUNT_TYPES } from '@/types/api';
import type { AccountType } from '@/types/api';

import BaseButton from '@/components/base/BaseButton.vue';
import BaseInput from '@/components/base/BaseInput.vue';
import BaseSelect from '@/components/base/BaseSelect.vue';
import BaseMessage from '@/components/base/BaseMessage.vue';
import FormField from '@/components/composite/FormField.vue';

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
    <FormField id="account-code" label="Code" :error="fieldErrors.code" v-slot="{ id }">
      <BaseInput :id="id" v-model="form.code" required maxlength="50" />
    </FormField>

    <FormField id="account-name" label="Name" :error="fieldErrors.name" v-slot="{ id }">
      <BaseInput :id="id" v-model="form.name" required maxlength="255" />
    </FormField>

    <FormField id="account-type" label="Type" :error="fieldErrors.type" v-slot="{ id }">
      <BaseSelect :id="id" v-model="form.type">
        <option v-for="type in ACCOUNT_TYPES" :key="type" :value="type">{{ type }}</option>
      </BaseSelect>
    </FormField>
    <BaseMessage v-if="formError" tone="error">{{ formError }}</BaseMessage>

    <BaseButton type="submit" :disabled="isSubmitting" class="self-start">
      Create account
    </BaseButton>
  </form>
</template>
