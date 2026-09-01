CREATE TABLE settled.ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key UUID UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ledger_account_id 
        FOREIGN KEY (account_id) REFERENCES settled.accounts(id) ON DELETE RESTRICT,
    CHECK (amount > 0)
);

CREATE INDEX idx_ledger_account_id ON settled.ledger(account_id);
CREATE INDEX idx_ledger_created_at ON settled.ledger(created_at);
CREATE INDEX idx_ledger_status ON settled.ledger(status);
CREATE INDEX idx_ledger_idempotency_key ON settled.ledger(idempotency_key);