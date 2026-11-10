import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import * as accountsApi from '@/api/accounts';
import { router } from '@/router';
import App from './App.vue';

vi.mock('@/api/accounts');

async function mountAppAt(path: string) {
  await router.push(path);
  await router.isReady();
  const wrapper = mount(App, { global: { plugins: [createPinia(), router] } });
  await flushPromises();
  return wrapper;
}

beforeEach(() => {
  vi.resetAllMocks();
  vi.mocked(accountsApi.listAccounts).mockResolvedValue([]);
});

describe('App shell', () => {
  it('renders the header, sidebar, footer and the routed page', async () => {
    const wrapper = await mountAppAt('/transactions');

    expect(wrapper.find('header').text()).toContain('Settled');
    expect(wrapper.find('aside').text()).toContain('Accounts');
    expect(wrapper.find('footer').exists()).toBe(true);
    expect(wrapper.find('h2').text()).toBe('Transactions');
  });

  it('redirects the root path to the accounts page', async () => {
    await mountAppAt('/');

    expect(router.currentRoute.value.path).toBe('/accounts');
  });

  it('navigates when a sidebar link is clicked', async () => {
    const wrapper = await mountAppAt('/transactions');

    await wrapper.find('a[href="/accounts"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/accounts');
    expect(wrapper.find('h2').text()).toBe('Accounts');
  });
});
