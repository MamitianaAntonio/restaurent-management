-- create unit_type enum
CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

-- Delete the old constraint on Ingredient
ALTER TABLE Ingredient DROP CONSTRAINT IF EXISTS fk_ingredient;
ALTER TABLE Ingredient DROP COLUMN IF EXISTS required_quantity;

-- create table DishIngredient
CREATE TABLE DishIngredient (
    id SERIAL PRIMARY KEY NOT NULL,
    id_dish int NOT NULL,
    id_ingredient int NOT NULL,
    quantity_required numeric(10,2),
    unit unit_type,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE CASCADE,
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id) ON DELETE CASCADE
);

-- add dish price in table Dish
ALTER TABLE Dish ADD COLUMN price numeric(10,2) DEFAULT 0;
GRANT ALL PRIVILEGES ON TABLE dishingredient TO mini_dish_db_manager ;

-- new insert in DishIngredient table
INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit)
VALUES
    (1, 1, 0.20, 'KG'),
    (1, 2, 0.15, 'KG'),
    (2, 3, 1.00, 'KG'),
    (2, 4, 0.30, 'L'),
    (4, 5, 0.20, 'KG');

-- update dish price
UPDATE dish
    SET price = 3500.00
    WHERE id = 1;

UPDATE dish
    SET price = 12000.00
    WHERE id = 2;

UPDATE dish
    SET price = 8000.00
    WHERE id = 4;

-- Stock movement
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE StockMovement (
    id INT PRIMARY KEY NOT NULL,
    id_ingredient INT NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit unit_type NOT NULL,
    type movement_type NOT NULL,
    creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient (id) ON DELETE CASCADE
);
