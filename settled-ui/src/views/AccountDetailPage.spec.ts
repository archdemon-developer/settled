import { describe, expect, it, vi } from 'vitest';
import { mount, RouterLinkStub } from '@vue/test-utils';
import AccountDetail from '../components/sections/accounts/AccountDetail.vue';
import AccountDetailPage from './AccountDetailPage.vue';

vi.mock('vue-router', () => ({ useRoute: () => ({ params: { id: '7' } }) }));

describe('AccountDetailPage', () => {
  it('links back to the list and passes the route id to the section', () => {
    const wrapper = mount(AccountDetailPage, {
      global: { stubs: { RouterLink: RouterLinkStub, AccountDetail: true } },
    });

    expect(wrapper.findComponent(RouterLinkStub).props('to')).toBe('/accounts');
    expect(wrapper.findComponent(AccountDetail).props('accountId')).toBe('7');
  });
});
