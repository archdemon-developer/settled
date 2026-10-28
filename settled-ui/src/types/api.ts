export type AccountType =
  'CHECKING' | 'SAVINGS' | 'MONEY MARKET' | 'CERTIFICATE OF DEPOSIT' | 'CREDIT CARD' | 'LOAN';

export type AccountStatus = 'ACTIVE' | 'ARCHIVED';

export type TransactionStatus = 'DRAFT' | 'POSTED' | 'REVERSED';

export type PostingDirection = 'DEBIT' | 'CREDIT';

export interface Account {
  id: string;
  code: string;
  name: string;
  type: AccountType;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface Transaction {
  id: string;
  reference: string;
  description: string | null;
  postedAt: string | null;
  status: TransactionStatus;
  createdAt: string;
  updatedAt: string | null;
}

export interface Posting {
  id: string;
  accountId: string;
  amount: string;
  direction: PostingDirection;
  createdAt: string;
}

export interface TransactionDetail extends Transaction {
  postings: Posting[];
}

export interface CreateAccountRequest {
  code: string;
  name: string;
  type: AccountType;
}

export interface CreateTransactionRequest {
  reference: string;
  description?: string;
  postedAt: string;
}

export interface CreatePostingRequest {
  accountId: string;
  amount: string;
  direction: PostingDirection;
}

export interface PaginationInfo {
  totalElements: number;
  page: number;
  pageSize: number;
}

export interface FieldViolation {
  field: string;
  message: string;
}

export interface ErrorDetails {
  errorCode: string;
  message: string;
  details?: string;
  field?: string;
  violations?: FieldViolation[];
}

export interface ResponseWrapper<T> {
  status: 'SUCCESS' | 'ERROR';
  code: number;
  timestamp: string;
  requestId: string;
  data?: T;
  error?: ErrorDetails;
  paginationInfo?: PaginationInfo;
}