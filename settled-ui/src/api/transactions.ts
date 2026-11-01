import { api } from './client';
import type {
  CreatePostingRequest,
  CreateTransactionRequest,
  Transaction,
  TransactionDetail,
  TransactionStatus,
} from '../types/api';

export function listTransactions(status?: TransactionStatus): Promise<Transaction[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return api.get<Transaction[]>(`/transactions${query}`);
}

export function getTransaction(transactionId: string): Promise<Transaction> {
  return api.get<Transaction>(`/transactions/${encodeURIComponent(transactionId)}`);
}

export function createTransaction(request: CreateTransactionRequest): Promise<Transaction> {
  return api.post<Transaction>('/transactions', request);
}

export function addPosting(
  transactionId: string,
  request: CreatePostingRequest,
): Promise<TransactionDetail> {
  return api.post<TransactionDetail>(
    `/transactions/${encodeURIComponent(transactionId)}/postings`,
    request,
  );
}

export function postTransaction(transactionId: string): Promise<Transaction> {
  return api.post<Transaction>(`/transactions/${encodeURIComponent(transactionId)}/post`);
}
