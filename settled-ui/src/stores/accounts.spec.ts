import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import * as accountsApi from '@/api/accounts';
import { ApiError } from '@/api/client';
import type { Account } from '@/types/api';
import { useAccountsStore } from './accounts';

vi.mock('@/api/accounts');

function account(overrides: Partial<Account> = {}): Account {
  return {
    id: '1',
    code: 'CHK001',
    name: 'Checking Account',
    type: 'CHECKING',
    status: 'ACTIVE',
    createdAt: '2026-11-02T10:00:00.000Z',
    updatedAt: null,
    ...overrides,
  };
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.resetAllMocks();
});

describe('accounts store', () => {
  it('replaces the accounts list with the fetched accounts', async () => {
    vi.mocked(accountsApi.listAccounts).mockResolvedValue([
      account({ id: '1' }),
      account({ id: '2' }),
    ]);
    const store = useAccountsStore();
    await store.fetchAccounts();
    expect(store.accounts.map(account => account.id)).toEqual(['1', '2']);
  });

  it('adds the created account and returns it', async () => {
    const created = account({ id: '9', code: 'SAV001' });
    vi.mocked(accountsApi.createAccount).mockResolvedValue(created);
    const store = useAccountsStore();
    const result = await store.createAccount({
      code: 'SAV001',
      name: 'Savings Account',
      type: 'SAVINGS',
    });
    expect(result).toEqual(created);
    expect(store.accounts).toEqual([created]);
  });

  it('leaves the accounts list unchanged if account creation fails', async () => {
    const failure = new ApiError(409, {
      errorCode: 'ACC_003',
      message: 'Code already exists',
      field: 'code',
    });
    vi.mocked(accountsApi.createAccount).mockRejectedValue(failure);
    const store = useAccountsStore();
    store.setAccounts([account({ id: '1' })]);
    const attempt = store.createAccount({ code: 'CHK001', name: 'Dup', type: 'CHECKING' });
    await expect(attempt).rejects.toBe(failure);
    expect(store.accounts).toHaveLength(1);
  });

  it('replaces the matching account when archived', async () => {
    const archived = account({ id: '1', status: 'ARCHIVED' });
    vi.mocked(accountsApi.archiveAccount).mockResolvedValue(archived);
    const store = useAccountsStore();
    store.setAccounts([account({ id: '1' }), account({ id: '2' })]);

    const result = await store.archiveAccount('1');

    expect(result).toEqual(archived);
    expect(store.accounts[0]?.status).toBe('ARCHIVED');
    expect(store.accounts[1]?.status).toBe('ACTIVE');
  });

  it('adds a single fetched account when it is not in the list', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account({ id: '5' }));
    const store = useAccountsStore();

    await store.fetchAccount('5');

    expect(store.accounts.map(a => a.id)).toEqual(['5']);
  });

  it('replaces a single fetched account when it is already in the list', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account({ id: '1', name: 'Renamed' }));
    const store = useAccountsStore();
    store.setAccounts([account({ id: '1', name: 'Old' })]);

    await store.fetchAccount('1');

    expect(store.accounts).toHaveLength(1);
    expect(store.accounts[0]?.name).toBe('Renamed');
  });

  it('exposes only active accounts through the getter', () => {
    const store = useAccountsStore();
    store.setAccounts([account({ id: '1' }), account({ id: '2', status: 'ARCHIVED' })]);

    expect(store.activeAccounts.map(a => a.id)).toEqual(['1']);
  });

  it('leaves the list unchanged when the archived account is not in it', async () => {
    vi.mocked(accountsApi.archiveAccount).mockResolvedValue(
      account({ id: '9', status: 'ARCHIVED' }),
    );
    const store = useAccountsStore();
    store.setAccounts([account({ id: '1' })]);

    const result = await store.archiveAccount('9');

    expect(result.status).toBe('ARCHIVED');
    expect(store.accounts.map(a => a.id)).toEqual(['1']);
  });
});
