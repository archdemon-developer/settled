# 0010. Frontend choices

**Status:** Accepted

## Context

The UI is a Vue 3 single-page app that talks to the backend through one API envelope. We wanted a small surface to maintain and tooling that actually works today.

## Decision

- **API client:** a thin wrapper around the platform `fetch` in `src/api/client.ts`. It unwraps the response envelope and throws a typed `ApiError` carrying the backend's error object. Axios was rejected as an extra dependency. Anything interceptor-like later (an auth header, handling 401) goes inside that one file, so switching clients touches only that file.
- **TypeScript is pinned to 6.0.x**, not the 7.0 originally planned. `vue-tsc` and `typescript-eslint` do not support TypeScript 7 yet. Revisit when TypeScript 7.1 tooling support lands.
- **Local build image:** the Dockerfile builder stage uses `node:26-slim` (glibc). The Alpine image failed `vite build` with a rolldown musl-binding error. The final stage is `nginx:alpine`.
- **Local serving:** in Docker Compose, nginx serves the built app on port 3000 and proxies `/api/` to the backend, so the browser sees a single origin.
- **Production:** the built `dist/` is static files on Cloud Storage behind Cloud CDN on GCP, not a running container. The Dockerfile exists for local parity only.
- **State and tests:** Pinia for state, Vitest and Vue Test Utils for tests.

## Consequences

- A small dependency footprint, and one place to change client behaviour.
- The TypeScript pin is a known, deliberate lag that needs an explicit revisit.
- Money fields are typed as strings to match ADR 0005.
