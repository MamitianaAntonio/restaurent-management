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
