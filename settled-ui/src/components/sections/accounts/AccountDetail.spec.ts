import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as accountsApi from '../../../api/accounts';
import { ApiError } from '../../../api/client';
import type { Account } from '../../../types/api';
import AccountDetail from './AccountDetail.vue';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises, mount } from '@vue/test-utils';

vi.mock('../../../api/accounts'); // delete the vue-router mock line

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
  return mount(AccountDetail, { props: { accountId: '1' }, global: { plugins: [pinia] } });
}

beforeEach(() => {
  vi.resetAllMocks();
});

describe('AccountDetail', () => {
  it('loads the account for the id in the route and shows its details', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account());

    const wrapper = mountSection();
    expect(wrapper.text()).toContain('Loading account');
    await flushPromises();

    expect(accountsApi.getAccount).toHaveBeenCalledWith('1');
    expect(wrapper.text()).toContain('CHK001');
    expect(wrapper.text()).toContain('Main Checking');
    expect(wrapper.text()).toContain('CHECKING');
  });

  it('archives an active account and hides the archive button', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account());
    vi.mocked(accountsApi.archiveAccount).mockResolvedValue(account({ status: 'ARCHIVED' }));
    const wrapper = mountSection();
    await flushPromises();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(accountsApi.archiveAccount).toHaveBeenCalledWith('1');
    expect(wrapper.text()).toContain('ARCHIVED');
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('offers no archive button for an account that is already archived', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account({ status: 'ARCHIVED' }));

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('shows the backend message when the account cannot be loaded', async () => {
    vi.mocked(accountsApi.getAccount).mockRejectedValue(
      new ApiError(404, { errorCode: 'ACC_001', message: 'Account not found' }),
    );

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.text()).toContain('Account not found');
    expect(wrapper.text()).not.toContain('Main Checking');
  });

  it('shows a generic message when the server cannot be reached', async () => {
    vi.mocked(accountsApi.getAccount).mockRejectedValue(new TypeError('Failed to fetch'));

    const wrapper = mountSection();
    await flushPromises();

    expect(wrapper.text()).toContain('Could not reach the server');
  });

  it('keeps the account visible and shows the error when archiving fails', async () => {
    vi.mocked(accountsApi.getAccount).mockResolvedValue(account());
    vi.mocked(accountsApi.archiveAccount).mockRejectedValue(
      new ApiError(500, { errorCode: 'SYS_001', message: 'Could not archive' }),
    );
    const wrapper = mountSection();
    await flushPromises();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain('Could not archive');
    expect(wrapper.text()).toContain('CHK001');
    expect(wrapper.find('button').exists()).toBe(true);
  });
});
