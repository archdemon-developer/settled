DROP INDEX IF EXISTS settled.idx_ledger_idempotency_key;
DROP INDEX IF EXISTS settled.idx_ledger_status;
DROP INDEX IF EXISTS settled.idx_ledger_created_at;
DROP INDEX IF EXISTS settled.idx_ledger_account_id;
DROP TABLE IF EXISTS settled.ledger;