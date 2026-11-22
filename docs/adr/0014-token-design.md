# 0014. Token design

**Status:** Accepted

## Context

ADR 0012 fixes the token contract the ledger accepts: signature from the issuer's JWKS, `iss`, an accepted `aud` (`settled-api` on Keycloak), `sub`, and a role claim. ADR 0013 puts a gateway in front of everything and makes the login a form in our app. Neither says how Keycloak is tuned to issue those tokens: how long they live, what refresh does, what goes inside them, and who may ask for one.

Keycloak's defaults do not fit a ledger. In the spike the default audience was `account`, and the default roles carried `offline_access` and `uma_authorization` into the token's role list.

We start with the smallest set of settings that is safe for a financial ledger, and record what we add later and what triggers each addition. This is the baseline we grow from, not something we replace.

Considered on 2026-11-22. Keycloak 26.8.2. Items not verified are listed at the end.

## Decision

### Baseline settings (Keycloak)

| Area                         | Setting                                                                                                                                                                               | Why                                                                                                         |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| Signing                      | RS256, realm default                                                                                                                                                                  | Spring's default. Same algorithm as the machine client's signed assertion, so one key type to reason about. |
| Access token lifetime        | 10 minutes, set explicitly at realm level                                                                                                                                             | Recorded in ADR 0013. It is also the revocation window (see Consequences). One number to change later.      |
| Audience                     | `settled-api`, from a dedicated client scope with an Audience mapper, added to every client that calls the ledger                                                                     | The ledger rejects any other audience (verified in the spike).                                              |
| Roles                        | `LEDGER_READ`, `LEDGER_WRITE`, `LEDGER_ADMIN` as realm roles, in `realm_access.roles`. None implies another                                                                           | Matches the key in ADR 0012's YAML and the role list there.                                                 |
| Claims                       | `iss`, `aud`, `sub`, `exp`, `iat`, roles. No email, name or profile claims in access tokens                                                                                           | The ledger does not need personal data, and tokens end up in logs and headers.                              |
| Human login (gateway client) | Confidential client. Direct access grants on, because the gateway's login adapter uses them (ADR 0013). Standard flow and implicit flow off. No other client has direct access grants | The login form is in our app. The grant is limited to the one client that needs it.                         |
| Refresh tokens (humans)      | Revoke Refresh Token on, Refresh Token Max Reuse 0, so each refresh token is used once. Held server-side by the gateway                                                               | Rotation limits what a stolen refresh token is worth.                                                       |
| Human sessions               | SSO Session Idle 30 minutes, SSO Session Max 8 hours. The gateway cookie session never outlives these                                                                                 | A working day, then log in again. These are our chosen values.                                              |
| Offline tokens               | Not used. Remove `offline_access` from the default roles, applied in BE.1.2. No client requests that scope                                                                            | Offline tokens outlive logout and do not expire by default, which is wrong for a ledger.                    |
| Machine callers              | One client per consumer. Service account, client_credentials, private_key_jwt with RS256, no refresh token. Assertion lifetime 60 seconds. Minimal roles per client                   | A leaked key affects one consumer and can be rotated alone. Private keys never go in the repo.              |
| Ledger validation            | Signature, `iss`, `aud`, `exp` (Spring defaults plus our audience check). Nothing else yet                                                                                            | Add claim checks when a claim carries a decision.                                                           |

**Decided later, at the gateway card:** how the gateway authenticates its own Keycloak client. The candidate is private_key_jwt, to match the machine clients. Spring Security's OAuth2 client documents support for it, but the direct grant call is made by our adapter, which would sign the assertion itself. It does not apply to Identity Platform, which uses an API key.

**Decided in ADR 0012, not here:** the production identity provider. On Identity Platform the same contract holds but the values differ: audience is the project ID, roles are custom claims (1000 byte limit, visible after the next token is issued), and the Firebase Auth REST sample shows a one-hour token. On that provider the gateway enforces the idle and maximum session limits itself, to design at the gateway card.

### Added later, and what triggers it

| Addition                                                          | Trigger                                                                                                                                                                                                                    |
| ----------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| MFA (TOTP)                                                        | Admins handle real money, or a policy requires it. Possible on both candidate providers through the login adapter.                                                                                                         |
| Passkeys                                                          | Required by a policy. Not available on the password sign-in path (unverified per provider), so it means the redirect login described in ADR 0013.                                                                          |
| `typ: at+jwt` header (RFC 9068)                                   | Keycloak can emit it and we want the ledger to refuse ID tokens or other JWTs as access tokens. A forum report says Spring's default decoder rejects `at+jwt`, so the type verifier must be configured first (unverified). |
| DPoP sender-constrained tokens (supported in Keycloak since 26.4) | External machine consumers, or any token handed outside our network. Check Spring resource server support first.                                                                                                           |
| Introspection or a revocation check on sensitive endpoints        | The 10 minute window is too long for some operation, such as admin actions or large postings.                                                                                                                              |
| Access token lifetime of 5 minutes                                | Gateway refresh is proven in practice. One-line change.                                                                                                                                                                    |
| Key rotation drill                                                | Before the first production deploy. Rotate the realm key in dev and prove the ledger accepts new tokens without a restart.                                                                                                 |
| Per-service audiences                                             | First service extraction (P36+). Each service validates its own audience. Token exchange to be evaluated then.                                                                                                             |
| Scopes or roles finer than the three                              | Roles stop mapping cleanly to permissions.                                                                                                                                                                                 |
| FAPI 2 profile (supported since Keycloak 26.4)                    | A regulator or partner requires it.                                                                                                                                                                                        |
| Lightweight access tokens                                         | Tokens grow too large.                                                                                                                                                                                                     |

### Considered and rejected for now

- **Opaque tokens with introspection on every call.** It adds a Keycloak call per request and a hard runtime dependency. JWT with JWKS keeps the ledger validating if Keycloak is briefly down. We bring introspection back only for sensitive endpoints.
- **Long-lived access tokens without refresh.** A stolen token would be valid too long.
- **Offline tokens.** See the table above.
- **Custom claims and mappers beyond audience and roles.** Nothing needs them yet.
- **A themed Keycloak login page.** Rejected as the default by the in-app form requirement (ADR 0013), and kept as the fallback if passkeys or a stricter policy are required.

## Consequences

- Revocation is bounded by the access token lifetime. Keycloak's "sign out all sessions" does not revoke outstanding access tokens, which must expire on their own (Keycloak 26.4 docs). A stolen access token works for up to 10 minutes. We accept that at this stage.
- These settings live in the Keycloak realm. Until a realm export is committed (follow-up card, with secrets and keys excluded), this ADR is the manual checklist.
- BE.1.2 tests cover: wrong audience gives 401, expired token gives 401, missing role gives 403, no `Set-Cookie` header, and a token holding only `offline_access` and `uma_authorization` gets no ledger authority.
- The gateway card implements login and refresh through the adapter, with rotation. A refresh failure forces a new login.
- The UI needs a session-expired path: 30 minutes idle or 8 hours total shows the login page.
- Direct access grants are on for one client, which makes that client's configuration security-critical. Its client authentication must be strong and its credentials must never reach the browser.

### Verified and not verified

Verified in the spike: the default audience is `account` and the Audience mapper fixes it; the ledger rejects a wrong audience; tokens for `settled-system` lasted 10 minutes after we changed the expiry in the client's Credentials tab; RS256 works end to end.

Verified from documentation: sign-out does not revoke access tokens; offline tokens survive logout and do not expire unless limited (26.2 docs); DPoP is supported from 26.4; RFC 9068 requires `typ: at+jwt`; Spring Security's OAuth2 client documents private_key_jwt; Identity Platform's sign-in returns an ID and refresh token and its custom claims are limited to 1000 bytes.

Not verified, to check before BE.1.2 closes:

- Which setting actually produced the 10 minute lifetime. We changed it in the Credentials tab. It should be set explicitly at realm or client level and re-measured as `exp - iat`.
- Refresh rotation behaviour with Max Reuse 0 on our Keycloak.
- Which `typ` our tokens carry in the header.
- That removing `offline_access` from the default roles has the intended effect.
- That client_credentials issues no refresh token by default.
- Keycloak's default SSO Session Idle and Max values (we set ours explicitly regardless).
- That the direct grant works for the gateway client with the settings above, with and without a `totp` parameter.
- Spring support for DPoP on the resource server.
- That the ledger picks up a rotated signing key without a restart.
