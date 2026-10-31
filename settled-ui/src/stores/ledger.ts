import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { Account, Transaction } from '../types/api';

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

  return {
    accounts,
    transactions,
    activeAccounts,
    draftTransactions,
    setAccounts,
    addAccount,
    setTransactions,
    addTransaction,
  };
});
