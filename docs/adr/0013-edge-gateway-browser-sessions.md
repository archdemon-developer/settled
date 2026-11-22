# 0013. Edge gateway and browser sessions

**Status:** Accepted

## Context

The admin UI is a browser app calling a financial ledger. ADR 0012 settles how tokens are issued and validated, but not how users log in or where the browser keeps its login. RFC 10017 (OAuth 2.0 for Browser-Based Applications, BCP 212) describes three patterns. A Backend for Frontend (BFF) keeps tokens on the server and gives the browser only a cookie session, and the RFC calls it the strongest pattern and recommends it for sensitive and business applications (section 6.1.4.3). A token-mediating backend keeps refresh tokens on the server but still hands access tokens to the page. A browser-only client holds both tokens in the browser and is exposed to every attack scenario in the RFC's threat section (section 6.3).

The master plan places microservices at P36 and later. Settled is a ledger as a service, so other systems call it too, not only the admin UI. We want one edge in front of everything from the start, so that every caller has a single front door and extraction later means adding routes, not moving a BFF out of the monolith.

**Product requirement:** the login form is part of our Vue app, not a page hosted by the identity provider. Admins never leave the app to log in.

Considered and discussed on 2026-11-22. Parts of RFC 10017 beyond section 6.3.3 (including token storage) were not read when this ADR was written.

## Decision

**A separate Spring gateway module, `settled-gateway`, sits in front of the ledger and is the only way in.** Every caller, human or machine, reaches the ledger through it. The ledger is not reachable from outside the network by any other path, and that is enforced by network configuration, not by convention.

**Human login.**

1. The Vue login form posts the username and password to the gateway over HTTPS.
2. The gateway signs in at the identity provider through a `LoginProvider` adapter and receives the tokens.
3. The gateway keeps the tokens server-side and gives the browser a session cookie: `HttpOnly` and `Secure` required, `SameSite=Strict` recommended (RFC 10017 section 6.1.3.2). The browser never holds a token.

```java
public interface LoginProvider {
    TokenSet login(String username, String password);
    TokenSet refresh(String refreshToken);
}
```

There is one adapter per provider: `KeycloakLoginProvider` (dev) calls Keycloak's token endpoint with the direct access grant, and `IdentityPlatformLoginProvider` calls Identity Platform's email and password sign-in. A Spring profile selects the adapter. Everything after the login (session, cookie, Bearer token, validation in the ledger) is identical for both. The interface is a sketch, to be finalised when the gateway is built.

**Rules for the login path.**

- HTTPS everywhere except plain `localhost` in development, because the password travels in that request.
- Passwords are never logged. Login request bodies are excluded from request logging and error reports.
- Direct access grants are enabled for the gateway's Keycloak client only, and stay off for every other client.
- Refresh goes through the adapter, so the browser never calls the provider.
- Logout ends the gateway session and the provider session.

**Relaying.** For each request the gateway replaces the session cookie with `Authorization: Bearer <access token>` and forwards to the ledger.

**Routing.** Today there is one route, `/api/**` to the ledger. Extracted services later add routes. The gateway holds no domain logic: routes, login and session only.

**Machine callers** also come through the gateway but do not use sessions. They get a token from the identity provider directly (signed JWT, ADR 0012), send it as a Bearer token, and the gateway passes it through untouched. The gateway does not validate it today. It can start to when per-client limits need its claims.

**The ledger always validates the JWT itself** (ADR 0012), whether the call came from a human through the gateway or from a machine. There is one authority mapping, not two.

**Request paths:** browser, then nginx (static files, same origin), then the gateway, then the ledger. Machine caller, then the gateway, then the ledger. The cookie only works if the UI and the gateway share one origin.

### Why a form in our app uses the password sign-in

The standard way to log in with OAuth is to redirect the browser to the provider's own page. We chose not to, so the gateway collects the credentials and passes them on. What we know about that:

- Verified: RFC 9700 (OAuth 2.0 Security Best Current Practice, section 2.4) says the resource owner password credentials grant MUST NOT be used, because it exposes the user's password to the client. Keycloak's direct access grant is that grant.
- Verified: Spring Authorization Server does not support the password grant, because it follows OAuth 2.1. Building our own server would not remove the problem. It would only mean writing the credential handling ourselves, which ADR 0012 defers.
- Verified: Identity Platform has a sign-in API meant for custom login screens. It returns an ID token and a refresh token, and has its own MFA calls (an MFA-pending credential on sign-in and a finalize call).
- Keycloak has no equivalent first-party sign-in API that we found. Its login is pages it hosts, styled with themes. Its API route for a username and password is the direct access grant.
- Reported on Keycloak forums and mailing lists, not in the official documentation: TOTP works with the direct grant through a `totp` request parameter. A missing code and a wrong password both return 401, so the gateway cannot tell which one to ask for without customising Keycloak.

So the gateway passing credentials is the native path on Identity Platform and the discouraged but working path on Keycloak. Because Keycloak is the dev provider and the production provider is still open (ADR 0012), the awkward side is limited to dev unless production also uses Keycloak.

### Rejected

- **Redirect to a provider-hosted login page**, themed to look like Settled. It is the standard flow, supports MFA and passkeys fully, and is the lowest-risk option. Rejected because the login form must live in our app. It is the fallback if requirements change, for example if admins need passkeys.
- **Vue calls the provider directly and holds the token** (the Firebase SDK pattern). The RFC lists browser-held tokens as exposed to every attack scenario. With Keycloak it would also need a browser-side client with the password grant enabled.
- **Browser-only SPA** (tokens in page memory). Same exposure, and we would have rewritten it later anyway.
- **BFF inside the monolith.** It would make the ledger both an OAuth2 client and a resource server, with a second filter chain and a second authority mapping for human logins. Moving it out later is a rewrite, which is the cost this decision avoids.
- **Token-mediating backend.** Access tokens would still reach page JavaScript.
- **Apigee or another managed gateway for the UI login.** Everything found describes them validating tokens that clients send, not holding tokens for a browser session. Not ruled out for quotas and rate limits at P16 and later, to be reassessed then.
- **Our own authorization server with the login page as a Vue route.** It would be a standard flow with our own form. Deferred with the rest of ADR 0012's own-server option.

**Gateway direction.** We write our own Spring gateway. It handles routing, login and session now. Rate limits, quotas and analytics are not built, and whether to build them or buy a product like Apigee is decided when they are needed. The reason to build is control and fit, not maturity: a product team's gateway is more mature than ours will be. Because every external consumer arrives here, the gateway is also the front door of the API product. Per-client limits, quotas and usage analytics belong at this point and are likely to be needed before P36 as external consumers appear, which makes the build-or-buy question more pressing than it first looked.

**Issuer portability.** For human login, replacing Keycloak with Google Identity Platform is a configuration change plus its adapter. It is not a configuration change for machine callers. Reported, not verified: Identity Platform has no client credentials grant and uses a different issuer from Google's service-account tokens, so machine login would need another mechanism and possibly multi-issuer validation (ADR 0012, GCP options).

## Consequences

- A second deployable now: `settled-gateway` needs its own pom, Dockerfile, CI job, Compose entry and tests, and the 90% coverage gate applies.
- P1 grows by one card (the gateway). FE.1.10 to FE.1.14 are rewritten around the login form and a cookie session. `src/api/client.ts` sends the cookie instead of attaching a token, and handles 401 by showing the login page. That is the single place ADR 0010 reserved for it.
- User passwords pass through gateway memory on every login. That is the price of the in-app form, and it is why the logging rule above is explicit.
- MFA depends on the provider. TOTP is possible on both candidates. Passkeys are not planned on this path, and unverified for each provider. If they are needed, the login moves to the redirect flow, and only the gateway and the login page change.
- Local and production use different adapters, so a bug can appear on one side only. Tests assert the token shape (issuer, audience, roles) against each provider's contract.
- Extraction later is adding routes. It does not move a BFF.
- Cookies are sent automatically, so the gateway needs CSRF protection on the cookie path and on the login endpoint.
- One more network hop and a new point of failure in front of the ledger.
- The ledger is no longer published to callers directly. Compose stops publishing its port to the host, and each environment needs an equivalent rule, with the mechanism chosen per environment.
- The identity provider stays directly reachable for machine token requests. For Keycloak, only through a proxy that exposes `/realms/` (ADR 0012).
- Amends ADR 0010. Its production plan (static files on Cloud Storage behind a CDN) must serve the UI and the gateway under one origin for the cookie to work. That needs a routing answer, such as a load balancer with path rules, when a GCP deployment is planned.

### Open items, not verified

- Which Spring Cloud release works with Spring Boot 4.1.x, and whether to use the Server MVC or the WebFlux gateway variant. The Server MVC docs list `TokenRelay`.
- The session store. Redis is already in Compose and is used in examples, but nothing here is confirmed.
- The CSRF mechanism for the cookie path and the login endpoint.
- The same-origin setup for production on GCP.
- How the gateway authenticates its own Keycloak client: private_key_jwt or a secret. To decide at the gateway card. Spring Security's OAuth2 client documents private_key_jwt support, but the direct grant call is made by our adapter, so it would sign the client assertion itself.
- That Keycloak's brute-force protection also covers the direct grant, and what Identity Platform does on repeated failures.
- How the gateway learns that a second factor is needed on Keycloak, given the 401 ambiguity above.
- How direct access to the ledger is blocked in each environment, in Compose and on GCP.
