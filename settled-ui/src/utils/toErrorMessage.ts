import { ApiError } from '@/api/client';

export function toErrorMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : 'Could not reach the server';
}
