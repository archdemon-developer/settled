import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import AppSidebar from './AppSidebar.vue';

const Stub = { template: '<div />' };

async function mountSidebarAt(path: string) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/accounts',
        children: [
          { path: '', component: Stub },
          { path: ':id', component: Stub },
        ],
      },
      { path: '/transactions', component: Stub },
    ],
  });

  await router.push(path);
  await router.isReady();

  return mount(AppSidebar, {
    global: {
      plugins: [router],
    },
  });
}

describe('AppSidebar', () => {
  it('links to the accounts and transactions pages', async () => {
    const wrapper = await mountSidebarAt('/accounts');

    const links = wrapper.findAll('a');

    expect(links.map((link) => link.text())).toEqual(['Accounts', 'Transactions']);
    expect(links.map((link) => link.attributes('href'))).toEqual(['/accounts', '/transactions']);
  });

  it('marks only the link for the current page as active', async () => {
    const wrapper = await mountSidebarAt('/transactions');

    expect(wrapper.find('a[href="/transactions"]').classes()).toContain('router-link-active');
    expect(wrapper.find('a[href="/accounts"]').classes()).not.toContain('router-link-active');
  });

  it('keeps the accounts link active on an account detail page', async () => {
    const wrapper = await mountSidebarAt('/accounts/abc-123');

    expect(wrapper.find('a[href="/accounts"]').classes()).toContain('router-link-active');
    expect(wrapper.find('a[href="/transactions"]').classes()).not.toContain('router-link-active');
  });
});
