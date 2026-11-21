CREATE TABLE invoices (
    id                  BIGSERIAL PRIMARY KEY,
    number              VARCHAR(20)   NOT NULL UNIQUE,
    customer_id         BIGINT        NOT NULL REFERENCES customers (id),
    currency            VARCHAR(3)    NOT NULL,
    status              VARCHAR(10)   NOT NULL DEFAULT 'DRAFT',
    issue_date          DATE          NOT NULL,
    payment_terms_days  INTEGER       NOT NULL DEFAULT 30,
    tax_rate            NUMERIC(5, 2) NOT NULL DEFAULT 0,
    discount_percent    NUMERIC(5, 2) NOT NULL DEFAULT 0,
    notes               TEXT,
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT invoices_status_check CHECK (status IN ('DRAFT', 'SENT', 'PAID', 'OVERDUE'))
);

CREATE TABLE invoice_items (
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   BIGINT         NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    position     INTEGER        NOT NULL,
    description  VARCHAR(500)   NOT NULL,
    quantity     NUMERIC(10, 2) NOT NULL,
    unit_price   NUMERIC(12, 2) NOT NULL
);

CREATE INDEX invoices_customer_idx ON invoices (customer_id);
CREATE INDEX invoices_status_idx ON invoices (status);
CREATE INDEX invoice_items_invoice_idx ON invoice_items (invoice_id);
