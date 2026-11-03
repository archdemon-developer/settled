import { afterEach, describe, expect, it, vi } from 'vitest';
import { archiveAccount, createAccount, getAccount, listAccounts } from './accounts';

function respondWith(data: unknown) {
  const body = { status: 'SUCCESS', code: 200, timestamp: '', requestId: 'req-1', data };
  const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(body), { status: 200 }));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('accounts api', () => {
  it('lists accounts without a filter', async () => {
    const fetchMock = respondWith([]);

    await listAccounts();

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/accounts', expect.anything());
  });

  it('adds the status filter to the query string', async () => {
    const fetchMock = respondWith([]);

    await listAccounts('ARCHIVED');

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/accounts?status=ARCHIVED', expect.anything());
  });

  it('encodes the account id in the path', async () => {
    const fetchMock = respondWith({ id: 'a b' });

    await getAccount('a b');

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/accounts/a%20b', expect.anything());
  });

  it('posts the new account as JSON', async () => {
    const fetchMock = respondWith({ id: '1' });
    const request = { code: 'CHK001', name: 'Main Checking', type: 'CHECKING' as const };

    await createAccount(request);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/accounts',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(request) }),
    );
  });

  it('archives with a PUT to the archive path', async () => {
    const fetchMock = respondWith({ id: '1' });

    await archiveAccount('1');

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/accounts/1/archive',
      expect.objectContaining({ method: 'PUT' }),
    );
  });
});
