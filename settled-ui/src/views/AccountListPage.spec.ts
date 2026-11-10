import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import AccountCreateForm from '../components/sections/accounts/AccountCreateForm.vue';
import AccountList from '../components/sections/accounts/AccountList.vue';
import AccountListPage from './AccountListPage.vue';

describe('AccountListPage', () => {
  it('shows the heading, the create form and the account list', () => {
    const wrapper = mount(AccountListPage, {
      global: { stubs: { AccountCreateForm: true, AccountList: true } },
    });

    expect(wrapper.find('h2').text()).toBe('Accounts');
    expect(wrapper.findComponent(AccountCreateForm).exists()).toBe(true);
    expect(wrapper.findComponent(AccountList).exists()).toBe(true);
  });
});
