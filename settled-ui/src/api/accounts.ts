import { api } from './client';
import type { Account, AccountStatus, CreateAccountRequest } from '../types/api';

export function listAccounts(status?: AccountStatus): Promise<Account[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return api.get<Account[]>(`/accounts${query}`);
}

export function getAccount(accountId: string): Promise<Account> {
  return api.get<Account>(`/accounts/${encodeURIComponent(accountId)}`);
}

export function createAccount(request: CreateAccountRequest): Promise<Account> {
  return api.post<Account>('/accounts', request);
}

export function archiveAccount(accountId: string): Promise<Account> {
  return api.put<Account>(`/accounts/${encodeURIComponent(accountId)}/archive`);
}
