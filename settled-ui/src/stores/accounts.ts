import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { Account, CreateAccountRequest } from '@/types/api';
import * as accountsApi from '@/api/accounts';

export const useAccountsStore = defineStore('accounts', () => {
  const accounts = ref<Account[]>([]);

  const activeAccounts = computed(() =>
    accounts.value.filter(account => account.status === 'ACTIVE'),
  );

  function setAccounts(next: Account[]) {
    accounts.value = next;
  }

  function addAccount(account: Account) {
    accounts.value.push(account);
  }

  async function fetchAccounts(): Promise<void> {
    const fetchedAccounts = await accountsApi.listAccounts();
    setAccounts(fetchedAccounts);
  }

  async function createAccount(request: CreateAccountRequest): Promise<Account> {
    const newAccount = await accountsApi.createAccount(request);
    addAccount(newAccount);
    return newAccount;
  }

  async function archiveAccount(accountId: string): Promise<Account> {
    const archivedAccount = await accountsApi.archiveAccount(accountId);
    const index = accounts.value.findIndex(account => account.id === accountId);
    if (index !== -1) {
      accounts.value[index] = archivedAccount;
    }
    return archivedAccount;
  }

  async function fetchAccount(accountId: string): Promise<Account> {
    const account = await accountsApi.getAccount(accountId);
    const index = accounts.value.findIndex(a => a.id === accountId);
    if (index === -1) {
      addAccount(account);
    } else {
      accounts.value[index] = account;
    }
    return account;
  }

  return {
    accounts,
    activeAccounts,
    setAccounts,
    addAccount,
    fetchAccounts,
    createAccount,
    archiveAccount,
    fetchAccount,
  };
});
