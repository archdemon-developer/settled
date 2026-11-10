import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import * as accountsApi from '@/api/accounts';
import { ApiError } from '@/api/client';
import { useLedgerStore } from '@/stores/ledger';
import type { Account } from '@/types/api';
import AccountCreateForm from './AccountCreateForm.vue';

vi.mock('@/api/accounts');

function account(overrides: Partial<Account> = {}): Account {
  return {
    id: '1',
    code: 'SAV001',
    name: 'Savings',
    type: 'SAVINGS',
    status: 'ACTIVE',
    createdAt: '2026-11-01T15:17:00.000Z',
    updatedAt: null,
    ...overrides,
  };
}

function mountForm() {
  const pinia = createPinia();
  setActivePinia(pinia);
  const wrapper = mount(AccountCreateForm, { global: { plugins: [pinia] } });
  return { wrapper, store: useLedgerStore(pinia) };
}

async function fillAndSubmit(wrapper: ReturnType<typeof mountForm>['wrapper']) {
  await wrapper.find('#account-code').setValue('SAV001');
  await wrapper.find('#account-name').setValue('Savings');
  await wrapper.find('#account-type').setValue('SAVINGS');
  await wrapper.find('form').trigger('submit');
  await flushPromises();
}

beforeEach(() => {
  vi.resetAllMocks();
});

describe('AccountCreateForm', () => {
  it('submits the entered values, adds the account and clears the fields', async () => {
    vi.mocked(accountsApi.createAccount).mockResolvedValue(account());
    const { wrapper, store } = mountForm();

    await fillAndSubmit(wrapper);

    expect(accountsApi.createAccount).toHaveBeenCalledWith({
      code: 'SAV001',
      name: 'Savings',
      type: 'SAVINGS',
    });
    expect(store.accounts).toHaveLength(1);
    expect(wrapper.find<HTMLInputElement>('#account-code').element.value).toBe('');
    expect(wrapper.find<HTMLInputElement>('#account-name').element.value).toBe('');
  });

  it('shows field-level violations from the backend next to the form', async () => {
    vi.mocked(accountsApi.createAccount).mockRejectedValue(
      new ApiError(400, {
        errorCode: 'VAL_001',
        message: 'Validation failed',
        violations: [{ field: 'name', message: 'Name is required' }],
      }),
    );
    const { wrapper } = mountForm();

    await fillAndSubmit(wrapper);

    expect(wrapper.text()).toContain('Name is required');
  });

  it('shows a duplicate code error returned for a single field', async () => {
    vi.mocked(accountsApi.createAccount).mockRejectedValue(
      new ApiError(409, { errorCode: 'ACC_003', message: 'Code already exists', field: 'code' }),
    );
    const { wrapper, store } = mountForm();

    await fillAndSubmit(wrapper);

    expect(wrapper.text()).toContain('Code already exists');
    expect(store.accounts).toHaveLength(0);
  });

  it('shows the backend message for an error without a field', async () => {
    vi.mocked(accountsApi.createAccount).mockRejectedValue(
      new ApiError(500, { errorCode: 'SYS_001', message: 'Something went wrong' }),
    );
    const { wrapper } = mountForm();

    await fillAndSubmit(wrapper);

    expect(wrapper.text()).toContain('Something went wrong');
  });

  it('shows a generic message when the server cannot be reached', async () => {
    vi.mocked(accountsApi.createAccount).mockRejectedValue(new TypeError('Failed to fetch'));
    const { wrapper } = mountForm();

    await fillAndSubmit(wrapper);

    expect(wrapper.text()).toContain('Could not reach the server');
  });

  it('disables the button while the request is in flight', async () => {
    let finish!: (created: Account) => void;
    vi.mocked(accountsApi.createAccount).mockReturnValue(
      new Promise<Account>((resolve) => {
        finish = resolve;
      }),
    );
    const { wrapper } = mountForm();
    const button = wrapper.find('button[type="submit"]');

    await wrapper.find('form').trigger('submit');
    expect(button.attributes('disabled')).toBeDefined();
    finish(account());
    await flushPromises();

    expect(button.attributes('disabled')).toBeUndefined();
  });

  it('shows a violation reported for the type field', async () => {
    vi.mocked(accountsApi.createAccount).mockRejectedValue(
      new ApiError(400, {
        errorCode: 'VAL_001',
        message: 'Validation failed',
        violations: [{ field: 'type', message: 'Type is required' }],
      }),
    );
    const { wrapper } = mountForm();

    await fillAndSubmit(wrapper);

    expect(wrapper.text()).toContain('Type is required');
  });
});
