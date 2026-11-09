import { describe, expect, it } from 'vitest';
import { router } from './index';
import AccountListPage from '../views/AccountListPage.vue';
import AccountDetailView from '../views/AccountDetailView.vue';
import TransactionListPage from '../views/TransactionListPage.vue';

function componentFor(path: string) {
  return router.resolve(path).matched.at(-1)?.components?.default;
}

describe('router', () => {
  it('resolves the accounts list page', () => {
    expect(componentFor('/accounts')).toBe(AccountListPage);
  });

  it('resolves the account detail page and exposes the id param', () => {
    const resolved = router.resolve('/accounts/abc-123');

    expect(resolved.matched.at(-1)?.components?.default).toBe(AccountDetailView);
    expect(resolved.params.id).toBe('abc-123');
  });

  it('resolves the transactions list page', () => {
    expect(componentFor('/transactions')).toBe(TransactionListPage);
  });

  it('redirects the root path to the accounts page', async () => {
    await router.push('/');

    expect(router.currentRoute.value.path).toBe('/accounts');
  });
});
