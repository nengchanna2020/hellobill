CREATE TABLE payments (
    id          BIGSERIAL PRIMARY KEY,
    invoice_id  BIGINT         NOT NULL REFERENCES invoices (id),
    amount      NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    paid_on     DATE           NOT NULL,
    method      VARCHAR(30)    NOT NULL,
    note        VARCHAR(500),
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX payments_invoice_idx ON payments (invoice_id);
