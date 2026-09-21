CREATE TABLE postings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
  account_id UUID NOT NULL REFERENCES accounts(id),
  amount NUMERIC(19,4) NOT NULL,
  direction VARCHAR(10) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_postings_transaction_id ON postings(transaction_id);
CREATE INDEX idx_postings_account_id ON postings(account_id);