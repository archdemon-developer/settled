import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, api } from './client';

function respondWith(status: number, body: unknown) {
  const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(body), { status }));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('api client', () => {
  it('unwraps data from a successful response', async () => {
    const fetchMock = respondWith(200, {
      status: 'SUCCESS',
      code: 200,
      timestamp: '2026-11-02T10:00:00.000Z',
      data: [{ id: '1' }],
    });

    const result = await api.get('/accounts');

    expect(result).toEqual([{ id: '1' }]);
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/accounts', expect.anything());
  });

  it('sends the request body as JSON on post', async () => {
    const fetchMock = respondWith(201, {
      status: 'SUCCESS',
      code: 201,
      timestamp: '2026-11-02T10:00:00.000Z',
      requestId: 'req-2',
      data: { id: '1' },
    });

    await api.post('/accounts', { code: 'CHK001' });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/accounts',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ code: 'CHK001' }) }),
    );
  });

  it('throws an ApiError carrying the backend error details', async () => {
    respondWith(409, {
      status: 'ERROR',
      code: 409,
      timestamp: '2026-11-02T10:00:00.000Z',
      requestId: 'req-3',
      error: { errorCode: 'ACC_003', message: 'Code already exists', field: 'code' },
    });

    const call = api.post('/accounts', { code: 'CHK001' });

    await expect(call).rejects.toBeInstanceOf(ApiError);
    await expect(call).rejects.toMatchObject({
      status: 409,
      error: { errorCode: 'ACC_003', field: 'code' },
    });
  });

  it('falls back to an unknown error when the error body is missing', async () => {
    respondWith(500, { status: 'ERROR', code: 500, timestamp: '', requestId: 'req-4' });

    const call = api.get('/accounts');

    await expect(call).rejects.toMatchObject({
      status: 500,
      error: { errorCode: 'UNKNOWN_ERROR' },
    });
  });

  it('throws an ApiError when the response is not JSON', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          new Response('<html>Bad Gateway</html>', { status: 502, statusText: 'Bad Gateway' }),
        ),
    );

    const call = api.get('/accounts');

    await expect(call).rejects.toMatchObject({
      status: 502,
      error: { errorCode: 'INVALID_RESPONSE' },
    });
  });

  it('falls back to the status text when an error body is not in the standard envelope', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ status: 404, error: 'Not Found', path: '/api/v1/nope' }), {
          status: 404,
          statusText: 'Not Found',
        }),
      ),
    );

    const call = api.get('/nope');

    await expect(call).rejects.toMatchObject({
      status: 404,
      error: { errorCode: 'UNKNOWN_ERROR', message: 'Not Found' },
    });
  });

  it('sends the request body as JSON on put', async () => {
    const fetchMock = respondWith(200, {
      status: 'SUCCESS',
      code: 200,
      timestamp: '2026-11-02T10:00:00.000Z',
      requestId: 'req-5',
      data: { id: '1' },
    });

    await api.put('/accounts/1', { name: 'Renamed' });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/accounts/1',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ name: 'Renamed' }) }),
    );
  });
});
