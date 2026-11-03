# 0008. Testing strategy

**Status:** Accepted

## Context

A ledger has to prove it works against the real database, not against a stand-in. An in-memory database with a different SQL dialect can pass while PostgreSQL behaves differently.

## Decision

Four layers:

1. **Service units:** Mockito with `MockitoExtension`. Prefer `ArgumentCaptor` and inspecting what was actually saved over loose `any()` stubbing, because capture catches bugs like a forgotten status.
2. **Controllers:** `@WebMvcTest` with `@MockitoBean`.
3. **Repositories:** run against a real PostgreSQL container with the real Flyway migrations.
4. **End to end:** `LedgerFlowIntegrationTests` starts the whole app on a random port and drives it with `RestTestClient`.

Supporting choices:
- **No H2.** It was removed entirely so there is one SQL dialect in the test path.
- **One shared container:** `PostgresContainerConfiguration` uses `@ServiceConnection`.
- **Test profile:** Flyway enabled and `ddl-auto: validate`, so the migrations are what actually gets tested.
- **JaCoCo gate:** 90% line coverage at `verify`. Models, repositories, exceptions and the application class are excluded.
- Test classes keep `@Autowired` field injection. This was a deliberate choice for simplicity.

## Consequences

- Tests need Docker running.
- The container image tag must match the Postgres major version used locally. An older image once failed the migrations because `gen_random_uuid()` was not available.
- `ddl-auto: validate` checks structure, not column defaults, so a wrong default can pass the suite. Defaults need to be reviewed by hand or covered by a test.
