import type { ErrorDetails, ResponseWrapper } from '../types/api';

export class ApiError extends Error {
  readonly status: number;
  readonly error: ErrorDetails;

  constructor(status: number, error: ErrorDetails) {
    super(error.message);
    this.status = status;
    this.error = error;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/v1${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  });

  let body: ResponseWrapper<T>;

  try {
    body = (await response.json()) as ResponseWrapper<T>;
  } catch {
    throw new ApiError(response.status, {
      errorCode: 'INVALID_RESPONSE',
      message: response.statusText || 'The server returned an unreadable response',
    });
  }

  if (body.status === 'ERROR' || !response.ok) {
    throw new ApiError(
      response.status,
      body.error ?? { errorCode: 'UNKNOWN_ERROR', message: response.statusText },
    );
  }

  return body.data as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'POST',
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'PUT',
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
};
