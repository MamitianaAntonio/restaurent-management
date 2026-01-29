CREATE TYPE payment_status AS ENUM ('UNPAID', 'PAID');

CREATE TABLE sale (
    id SERIAL PRIMARY KEY NOT NULL,
    sale_datetime Timestamp NOT NULL DEFAULT now(),
    id_order int,
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES "order"(id)
);