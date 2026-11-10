import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import * as accountsApi from '../../../api/accounts.ts';
import { ApiError } from '../../../api/client.ts';
import type { Account } from '../../../types/api.ts';
import AccountList from './AccountList.vue';

vi.mock('../../../api/accounts');

function account(overrides: Partial<Account> = {}): Account {
  return {
    id: '1',
    code: 'CHK001',
    name: 'Main Checking',
    type: 'CHECKING',
    status: 'ACTIVE',
    createdAt: '2026-11-01T15:17:00.000Z',
    updatedAt: null,
    ...overrides,
  };
}

function mountSection() {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(AccountList, {
    global: { plugins: [pinia], stubs: { RouterLink: RouterLinkStub } },
  });
}

beforeEach(() => {
  vi.resetAllMocks();
});

describe('AccountList', () => {
  it('shows a loading message and then one row per account', async () => {
    vi.mocked(accountsApi.listAccounts).mockResolvedValue([
      account({ id: '1', code: 'CHK001' }),
      account({ id: '2', code: 'SAV001', name: 'Savings', type: 'SAVINGS' }),
    ]);

    const wrapper = mountSection();
    expect(wrapper.text()).toContain('Loading accounts');
    await flushPromises();

    expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    expect(wrapper.text()).toContain('CHK001');
    expect(wrapper.text()).toContain('SAV001');
  });

  it('links each account code to its detail page', async () => {
    vi.mocked(accountsApi.listAccounts).mockResolvedValue([account({ id: 'abc-123' })]);

    const wrapper = mountSection();
    await flushPromises();

    const link = wrapper.findAllComponents(RouterLinkStub)[0];
    expect(link?.props('to')).toBe('/accounts/abc-123');
  });

  it('shows an empty state when there are no accounts', async () => {
    vi.mocked(accountsApi.listAccounts).mockResolvedValue([]);

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.text()).toContain('No accounts yet.');
    expect(wrapper.find('table').exists()).toBe(false);
  });

  it('shows the backend message when the API returns an error', async () => {
    vi.mocked(accountsApi.listAccounts).mockRejectedValue(
      new ApiError(500, { errorCode: 'SYS_001', message: 'Something went wrong' }),
    );

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.text()).toContain('Something went wrong');
    expect(wrapper.find('table').exists()).toBe(false);
  });

  it('shows a generic message when the server cannot be reached', async () => {
    vi.mocked(accountsApi.listAccounts).mockRejectedValue(new TypeError('Failed to fetch'));

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.text()).toContain('An unexpected error occurred');
  });
});
