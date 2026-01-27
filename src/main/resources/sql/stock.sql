-- Stock movement
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

-- create stock movement table
CREATE TABLE StockMovement (
   id INT PRIMARY KEY NOT NULL,
   id_ingredient INT NOT NULL,
   quantity NUMERIC(10, 2) NOT NULL,
   unit unit_type NOT NULL,
   type movement_type NOT NULL,
   creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient (id) ON DELETE CASCADE
);

-- add new stock movement insert
insert into StockMovement values
   (1, 1, 5.0, 'KG', 'IN', '2024-01-05 08:00'),
   (2, 1, 0.2, 'KG', 'OUT', '2024-01-06 12:00'),
   (3, 2, 4.0, 'KG', 'IN', '2024-01-05 08:00'),
   (4, 2, 0.15, 'KG', 'OUT', '2024-01-06 12:00'),
   (5, 3, 10.00, 'KG', 'IN', '2024-01-04 09:00'),
   (6, 3, 1.0, 'KG', 'OUT', '2024-01-06 13:00'),
   (7, 4, 3.0, 'KG', 'IN', '2024-01-05 10:00'),
   (8, 4, 0.3, 'KG', 'OUT', '2024-01-06 14:00'),
   (9, 5, 2.5, 'KG', 'IN', '2024-01-05 10:00'),
   (10, 5, 0.2, 'KG', 'OUT', '2024-01-06 14:00');
