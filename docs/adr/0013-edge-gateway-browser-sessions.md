# 0013. Edge gateway and browser sessions

**Status:** Accepted

## Context

The admin UI is a browser app calling a financial ledger. ADR 0012 settles how tokens are issued and validated, but not where the browser keeps its login. RFC 10017 (OAuth 2.0 for Browser-Based Applications, BCP 212) describes three patterns. A Backend for Frontend (BFF) keeps tokens on the server and gives the browser only a cookie session, and the RFC calls it the strongest pattern and recommends it for sensitive and business applications (section 6.1.4.3). A token-mediating backend keeps refresh tokens on the server but still hands access tokens to the page. A browser-only client holds both tokens in the browser and is exposed to every attack scenario in the RFC's threat section (section 6.3).

The master plan places microservices at P36 and later. Settled is a ledger as a service, so other systems call it too, not only the admin UI. We want one edge in front of everything from the start, so that every caller has a single front door and extraction later means adding routes, not moving a BFF out of the monolith.

Considered and discussed on 2026-11-22. Parts of the RFC beyond section 6.3.3 (including token storage) were not read when this ADR was written.

## Decision

**A separate Spring gateway module, `settled-gateway`, sits in front of the ledger and is the only way in.** Every caller, human or machine, reaches the ledger through it. The ledger is not reachable from outside the network by any other path, and that is enforced by network configuration, not by convention.

- **Browser login.** The gateway is a confidential OAuth2 client of Keycloak using the authorization code grant (RFC 6.1.3.1). It keeps the tokens server-side and gives the browser a session cookie: `HttpOnly` and `Secure` required, `SameSite=Strict` recommended (RFC 6.1.3.2). The browser never holds a token.
- **Relaying.** For each request it replaces the session cookie with `Authorization: Bearer <access token>` and forwards to the ledger (Spring Cloud Gateway's `TokenRelay` filter).
- **Routing.** Today there is one route, `/api/**` to the ledger. Extracted services later add routes. The gateway holds no domain logic: routes, login and session only.
- **Machine callers** also come through the gateway but do not use sessions. They get a token from Keycloak directly (signed JWT, ADR 0012), send it as a Bearer token, and the gateway passes it through untouched. The gateway does not validate it today. It can start to when per-client limits need its claims.
- **The ledger always validates the JWT itself** (ADR 0012), whether the call came from a human through the gateway or from a machine. There is one authority mapping, not two.
- **Logout** ends both the gateway session and the Keycloak session.

**Request paths:** browser, then nginx (static files, same origin), then the gateway, then the ledger. Machine caller, then the gateway, then the ledger. The cookie only works if the UI and the gateway share one origin.

**Rejected:**

- **Browser-only SPA** (tokens in page memory). The RFC lists it as exposed to every attack scenario, and we would have rewritten it later anyway.
- **BFF inside the monolith.** It would make the ledger both an OAuth2 client and a resource server, with a second filter chain and a second authority mapping for human logins. Moving it out later is a rewrite, which is the cost this decision avoids.
- **Token-mediating backend.** Access tokens would still reach page JavaScript.
- **Apigee or another managed gateway for the UI login.** Everything found describes them validating tokens that clients send, not holding tokens for a browser session. Not ruled out for quotas and rate limits at P16 and later, to be reassessed then.

**Gateway direction.** We write our own Spring gateway. It handles routing, login and session now. Rate limits, quotas and analytics are not built, and whether to build them or buy a product like Apigee is decided when they are needed. The reason to build is control and fit, not maturity: a product team's gateway is more mature than ours will be. Because every external consumer arrives here, the gateway is also the front door of the API product. Per-client limits, quotas and usage analytics belong at this point and are likely to be needed before P36 as external consumers appear, which makes the build-or-buy question more pressing than it first looked.

**Issuer portability.** The gateway is an ordinary OIDC client, so replacing Keycloak with Google Identity Platform for human login is a configuration change. It is not a configuration change for machine callers. Reported, not verified: Identity Platform has no client credentials grant and uses a different issuer from Google's service-account tokens, so machine login would need another mechanism and possibly multi-issuer validation (ADR 0012, GCP options).

## Consequences

- A second deployable now: `settled-gateway` needs its own pom, Dockerfile, CI job, Compose entry and tests, and the 90% coverage gate applies.
- P1 grows by one card (the gateway). FE.1.10 to FE.1.14 are rewritten around a cookie session. `src/api/client.ts` sends the cookie instead of attaching a token, and handles 401 by redirecting to login. That is the single place ADR 0010 reserved for it.
- Extraction later is adding routes. It does not move a BFF.
- Cookies are sent automatically, so the gateway needs CSRF protection on the cookie path.
- One more network hop and a new point of failure in front of the ledger.
- The ledger is no longer published to callers directly. Compose stops publishing its port to the host, and each environment needs an equivalent rule, with the mechanism chosen per environment.
- Keycloak stays directly reachable for token requests. Machine callers get their tokens there, not through the gateway.
- Amends ADR 0010. Its production plan (static files on Cloud Storage behind a CDN) must serve the UI and the gateway under one origin for the cookie to work. That needs a routing answer, such as a load balancer with path rules, when a GCP deployment is planned.

### Open items, not verified

- Which Spring Cloud release works with Spring Boot 4.1.x, and whether to use the Server MVC or the WebFlux gateway variant. The Server MVC docs list `TokenRelay`.
- The session store. Redis is already in Compose and is used in examples, but nothing here is confirmed.
- The CSRF mechanism for the cookie path.
- The same-origin setup for production on GCP.
- Where the gateway's Keycloak client is registered and how its secret is held.
- How direct access to the ledger is blocked in each environment, in Compose and on GCP.
