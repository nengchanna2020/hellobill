CREATE TABLE recurring_plans (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT         NOT NULL REFERENCES customers (id),
    description    VARCHAR(500)   NOT NULL,
    amount         NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    tax_rate       NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    next_run_date  DATE           NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

ALTER TABLE invoices ADD COLUMN recurring_plan_id BIGINT REFERENCES recurring_plans (id);
