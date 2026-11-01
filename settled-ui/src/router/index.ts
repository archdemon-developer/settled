import { createRouter, createWebHistory } from 'vue-router';
import AccountListPage from '../views/AccountListPage.vue';
import AccountDetailView from '../views/AccountDetailView.vue';
import TransactionListPage from '../views/TransactionListPage.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/accounts' },
    { path: '/accounts', component: AccountListPage },
    { path: '/accounts/:id', component: AccountDetailView },
    { path: '/transactions', component: TransactionListPage },
  ],
});
