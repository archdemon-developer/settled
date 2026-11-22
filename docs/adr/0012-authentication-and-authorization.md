# 0012. Authentication and authorization

**Status:** Accepted

## Context

P1 secures the ledger API. Getting authentication wrong is expensive and hard to undo, so we do not write credential handling ourselves: no password storage, no token issuing, no session handling in the ledger.

The issuer will change over the project's life. Locally it is Keycloak in Docker Compose (P0 to P15). A GCP deployment comes later and may use a different identity service. The ledger code must not care which one issued a token.

Considered on 2026-11-22:

- **Build our own authorization server** (Spring Authorization Server in a `settled-auth` service with its own database). It supplies the protocol endpoints, but we would still write login, password reset, lockout, MFA, key rotation, an admin UI, audit logging and hardening, and own the patching. That is weeks for a first version plus permanent security upkeep, on a financial ledger. **Deferred, not rejected.** The token contract below keeps the ledger, the gateway and every client unchanged if we swap the issuer later.
- **Keycloak** for local development and **Google Identity Platform** as a candidate for GCP (see below).

A one-hour spike on 2026-11-21 (Keycloak 26.8.2, a throwaway Spring Boot 4.1.2 resource server) tested the design below. Everything marked "verified" was observed there. Everything marked "not verified" was not.

How users log in and where tokens live is ADR 0013. How Keycloak is tuned to issue the tokens is ADR 0014.

## Decision

**The ledger API is an OAuth2 resource server.** It validates bearer JWTs and never issues them. It has no login endpoint, no user table and no HTTP session. Spring Security does the validation from configuration.

**Token contract.** Any issuer must provide tokens with:

| Claim        | Requirement                                                                        | Checked by                                                               |
| ------------ | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| `iss`        | Equals the configured issuer URI                                                   | Spring, verified                                                         |
| `aud`        | Contains an audience the ledger is configured to accept: `settled-api` on Keycloak | Spring, verified (a token for another audience gets 401 `invalid_token`) |
| `sub`        | Stable, unique id of the caller                                                    | Not checked, used as the principal name                                  |
| a role claim | A list of strings yielding the roles below                                         | Mapped to authorities, verified                                          |
| signature    | Verifiable from the issuer's JWKS endpoint                                         | Spring, verified (RS256 by default)                                      |

**Roles.** Three, named as authorities:

| Authority      | Allows                                                   |
| -------------- | -------------------------------------------------------- |
| `LEDGER_READ`  | Read endpoints (GET)                                     |
| `LEDGER_WRITE` | Mutating endpoints (create, add postings, post, archive) |
| `LEDGER_ADMIN` | Operational and administrative endpoints                 |

Roles do not imply each other. A caller that needs read and write is assigned both. Each endpoint checks exactly one role. Which endpoint needs which is decided in BE.1.3. More roles are added only when a real caller needs a finer split, which is a Keycloak change plus role checks and leaves the token contract alone.

**Configuration.** The same keys are used in every profile. Only the values change per issuer:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/settled
          jwk-set-uri: http://localhost:8180/realms/settled/protocol/openid-connect/certs
          audiences:
            - settled-api
          authority-prefix: ""
          authorities-claim-expressions:
            - "[realm_access][roles]"
```

- Verified: `issuer-uri`, `jwk-set-uri`, `audiences`, `authority-prefix` and `authorities-claim-expressions` are all real Boot 4.1 keys and work from YAML alone. No converter bean is needed.
- Without `authority-prefix: ""`, authorities arrive as `SCOPE_LEDGER_READ`. The empty prefix gives `LEDGER_READ`.
- Setting both `issuer-uri` and `jwk-set-uri` means no startup request to the issuer, and `iss` is still validated.
- Spring Boot silently ignores unknown keys. A typo here fails open to default behaviour, not with an error. See the parity test below.
- `audiences` and `[realm_access][roles]` are the issuer-specific values. Another issuer supplies its own audience and role claim path.

**Local issuer.** Keycloak `26.8.2` in Docker Compose, `start-dev`, realm `settled`, published only on `127.0.0.1:8180`.

**Machine callers.** One confidential client per consumer (the spike used `settled-system`), the client credentials grant, and realm roles on its service account. The client authenticates with a signed JWT (private_key_jwt, RS256): there is no shared secret, and Keycloak holds only the public key. Verified: the assertion's `aud` is the realm issuer URL, a token request signed with a stranger's key is refused (`invalid_client`), and a client configured for ES256 with an RSA key gets no token. RS256 is the recorded choice. Private keys never go in the repository.

**Keycloak exposure.** Only `/realms/` is reachable from outside, through the proxy in ADR 0013. The admin console (`/admin/`) and the management port (9000) are never exposed. Administration happens on an internal route or a separate hostname.

**Profiles.** `application-dev.yaml` holds the Keycloak values. `application-gcp.yaml` is a placeholder with the same keys, `${...}` values with no defaults, and a comment saying it is unverified. `gcp` layers on top of `prod` (`prod,gcp`). A test asserts that both files have the same set of keys.

**GCP issuer: not decided.** Two options are open, to be chosen when a GCP deployment is planned:

- **A. Identity Platform plus Google service-account tokens.** A managed service. Verified from Google's documentation: its email and password sign-in API returns an ID token and a refresh token; ID tokens carry `https://securetoken.google.com/<projectId>` as issuer and the project id as audience; roles can be set as custom claims (limited to 1000 bytes, and visible only in tokens issued after they are set); TOTP MFA is supported; the Firebase Auth REST documentation's sample response shows a one-hour token. Reported, not verified: it has no client credentials grant, so machine callers would use Google service-account tokens (issuer `https://accounts.google.com`), which means two issuers and multi-issuer validation code. Its sign-in API suits a login form inside our own app, which fits ADR 0013 without further work.
- **B. Keycloak on Cloud Run with Cloud SQL.** More to operate, but the `gcp` profile stays a pure value swap, one issuer serves humans and machines, and the token contract is unchanged. Its login path is the password grant described in ADR 0013, which would then run in production.

The ledger sees the same contract either way. The gateway's login adapter is the only code that differs (ADR 0013).

**Explicitly not built in P1.** Our own authorization server, login or password code in the ledger, token introspection, scopes beyond roles, offline tokens, sender-constrained tokens (DPoP) and MFA. The first additions and their triggers are listed in ADR 0014.

## Findings the next tasks depend on

**For BE.1.2 (security configuration)**

- Add `spring-boot-starter-security-oauth2-resource-server`. The test starter `spring-boot-starter-security-oauth2-resource-server-test` was also generated by Initializr.
- Define our own `SecurityFilterChain` with a stateless session policy. The default chain sets a `JSESSIONID` cookie on an unauthenticated request, which a stateless API must not do. Add a test asserting no `Set-Cookie`.
- Verified: no token gives 401 with a bare `WWW-Authenticate: Bearer`. A token for the wrong audience gives 401 with `invalid_token` and "The aud claim is not valid". Both have an empty body, so mapping 401 and 403 onto the ADR 0006 envelope is still to do.
- Not verified: the Compose host split, where the backend container reaches `keycloak:8080` while tokens carry `localhost:8180`. Setting `issuer-uri` and `jwk-set-uri` separately should handle it. Verify it with the real backend container.

**For BE.1.3 (roles and method security)**

- Authorities come from the token's role claim, unprefixed. The token also carries Keycloak's own roles (`default-roles-settled`, `offline_access`, `uma_authorization`). They become authorities too and must never be used in access rules. ADR 0014 removes `offline_access` from the defaults.

**For the gateway and UI login (ADR 0013)**

- Keycloak's default `aud` is `account`. The realm needs an Audience mapper (included custom audience `settled-api`, added to the access token) on each client that calls the API. Without it every token is rejected.
- Keycloak's default access token lifetime is 5 minutes (observed). ADR 0014 sets the value we use.
- Not spiked: the human login flow.

## Consequences

- No credential handling in the ledger. A bug in Keycloak is not a bug in Settled, but patching Keycloak becomes our job. Keycloak 26.8.2-1 and 26.8.2-2 shipped a Java runtime affected by CVE-2026-114938. `26.8.2` currently resolves to a fixed build (OpenJDK 21.0.13.1); the image tag should be pinned and its Java version checked when it is bumped.
- Realm configuration (clients, roles, audience mapper, token settings) lives in Keycloak, not in code. For now it is set up by hand, with ADR 0014 as the checklist. Exporting the realm and importing it at startup with `--import-realm` is a follow-up so that a fresh clone works without clicking.
- Because Spring ignores unknown keys, a misspelled security key would silently weaken auth. The key-parity test catches a missing key in `application-gcp.yaml`, and the BE.1.2 integration tests must assert real behaviour (401 without a token, 401 for a wrong audience, 403 for a missing role, 200 with the right authority).
- The GCP choice is postponed. Until then `application-gcp.yaml` is untested and must not be deployed.
