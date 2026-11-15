import { describe, expect, it } from 'vitest';
import { ApiError } from '@/api/client';
import { toErrorMessage } from './toErrorMessage';

describe('toErrorMessage', () => {
  it('uses the message the backend sent', () => {
    const error = new ApiError(404, { errorCode: 'ACC_001', message: 'Account not found' });
    expect(toErrorMessage(error)).toBe('Account not found');
  });

  it('says the server could not be reached for any other error', () => {
    expect(toErrorMessage(new TypeError('Failed to fetch'))).toBe('Could not reach the server');
    expect(toErrorMessage('something odd')).toBe('Could not reach the server');
  });
});
