CREATE TABLE idempotency_keys (
  key VARCHAR(255) PRIMARY KEY,
  transaction_id UUID NOT NULL REFERENCES transactions(id),
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys(expires_at);