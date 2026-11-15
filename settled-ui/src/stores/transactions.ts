import { ref } from 'vue';
import { defineStore } from 'pinia';
import type { Transaction } from '@/types/api';

export const useTransactionsStore = defineStore('transactions', () => {
  const transactions = ref<Transaction[]>([]);

  function setTransactions(next: Transaction[]) {
    transactions.value = next;
  }

  function addTransaction(transaction: Transaction) {
    transactions.value.push(transaction);
  }

  return {
    transactions,
    setTransactions,
    addTransaction,
  };
});
