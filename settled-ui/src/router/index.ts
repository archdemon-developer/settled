import { createRouter, createWebHistory } from 'vue-router';
import AccountListPage from '@/views/AccountListPage.vue';
import AccountDetailPage from '@/views/AccountDetailPage.vue';
import TransactionListPage from '@/views/TransactionListPage.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/accounts' },
    {
      path: '/accounts',
      children: [
        { path: '', component: AccountListPage },
        { path: ':id', component: AccountDetailPage },
      ],
    },
    { path: '/transactions', component: TransactionListPage },
  ],
});
