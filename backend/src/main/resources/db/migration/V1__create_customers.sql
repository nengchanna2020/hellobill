CREATE TABLE customers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    email       VARCHAR(320) NOT NULL,
    country     VARCHAR(2)   NOT NULL,
    currency    VARCHAR(3)   NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX customers_name_idx ON customers (lower(name));
