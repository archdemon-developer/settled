import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  addPosting,
  createTransaction,
  getTransaction,
  listTransactions,
  postTransaction,
} from './transactions';

function respondWith(data: unknown) {
  const body = { status: 'SUCCESS', code: 200, timestamp: '', requestId: 'req-1', data };
  const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(body), { status: 200 }));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('transactions api', () => {
  it('lists transactions with and without a status filter', async () => {
    const fetchMock = respondWith([]);

    await listTransactions();
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 'SUCCESS', data: [] })));
    await listTransactions('DRAFT');

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/transactions', expect.anything());
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/v1/transactions?status=DRAFT',
      expect.anything(),
    );
  });

  it('gets a single transaction by id', async () => {
    const fetchMock = respondWith({ id: '1' });

    await getTransaction('1');

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/transactions/1', expect.anything());
  });

  it('creates a transaction with a POST', async () => {
    const fetchMock = respondWith({ id: '1' });
    const request = { reference: 'TXN001', postedAt: '2026-11-01T10:00:00.000Z' };

    await createTransaction(request);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/transactions',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(request) }),
    );
  });

  it('sends the posting amount as a string', async () => {
    const fetchMock = respondWith({ id: '1', postings: [] });
    const request = { accountId: 'acc-1', amount: '100.50', direction: 'DEBIT' as const };

    await addPosting('1', request);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/transactions/1/postings',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(request) }),
    );
  });

  it('posts a transaction through the post endpoint', async () => {
    const fetchMock = respondWith({ id: '1' });

    await postTransaction('1');

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/transactions/1/post',
      expect.objectContaining({ method: 'POST' }),
    );
  });
});
