-- order table
CREATE TABLE "order" (
    id serial PRIMARY KEY NOT NULL,
    reference VARCHAR(8) NOT NULL,
    creation_datetime TIMESTAMP DEFAULT now()
);

-- dish order table
CREATE TABLE dish_order(
    id       SERIAL PRIMARY KEY NOT NULL,
    id_order int,
    id_dish  int,
    quantity int NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES "order"(id) ON DELETE CASCADE,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES dish(id) ON DELETE CASCADE
);

insert into "order" (id, reference, creation_datetime) values
  (1, 'ORD00001', '2024-01-01 00:00'),
  (2, 'ORD00002', '2024-01-02 00:00');


insert into dish_order (id_order, id_dish, quantity) values
(1, 1, 2),
(1, 2, 2),
(2, 3, 1);
