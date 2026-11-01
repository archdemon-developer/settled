import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { Account, CreateAccountRequest, Transaction } from '../types/api';
import * as accountsApi from '../api/accounts';

export const useLedgerStore = defineStore('ledger', () => {
  const accounts = ref<Account[]>([]);
  const transactions = ref<Transaction[]>([]);

  const activeAccounts = computed(() =>
    accounts.value.filter((account) => account.status === 'ACTIVE'),
  );

  const draftTransactions = computed(() =>
    transactions.value.filter((transaction) => transaction.status === 'DRAFT'),
  );

  function setAccounts(next: Account[]) {
    accounts.value = next;
  }

  function addAccount(account: Account) {
    accounts.value.push(account);
  }

  function setTransactions(next: Transaction[]) {
    transactions.value = next;
  }

  function addTransaction(transaction: Transaction) {
    transactions.value.push(transaction);
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
    const index = accounts.value.findIndex((account) => account.id === accountId);
    if (index !== -1) {
      accounts.value[index] = archivedAccount;
    }
    return archivedAccount;
  }

  return {
    accounts,
    transactions,
    activeAccounts,
    draftTransactions,
    setAccounts,
    addAccount,
    setTransactions,
    addTransaction,
    fetchAccounts,
    createAccount,
    archiveAccount,
  };
});
