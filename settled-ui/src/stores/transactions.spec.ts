import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import type { Transaction } from '@/types/api';
import { useTransactionsStore } from './transactions';

function transaction(overrides: Partial<Transaction> = {}): Transaction {
  return {
    id: '1',
    reference: '1',
    description: 'Deposit',
    postedAt: '2026-11-02T10:00:00.000Z',
    status: 'POSTED',
    createdAt: '2026-11-02T10:00:00.000Z',
    updatedAt: null,
    ...overrides,
  };
}

beforeEach(() => {
  setActivePinia(createPinia());
});

describe('Transactions store', () => {
  it('exposes only draft transactions through the getter', () => {
    const store = useTransactionsStore();
    store.setTransactions([
      transaction({ id: '1', status: 'DRAFT' }),
      transaction({ id: '2', status: 'POSTED' }),
    ]);

    expect(store.transactions.map(transaction => transaction.id)).toEqual(['1', '2']);
  });

  it('adds a transaction to the list', () => {
    const store = useTransactionsStore();

    store.addTransaction(transaction({ id: '7' }));

    expect(store.transactions.map(transaction => transaction.id)).toEqual(['7']);
  });
});
