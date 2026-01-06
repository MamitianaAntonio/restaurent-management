CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');

-- create table ingredient
CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    price numeric(10,2),
    category category_enum,
    id_dish int,
    CONSTRAINT fk_ingredient
        FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE CASCADE
);

-- create table dish
CREATE TABLE Dish (
    id SERIAL PRIMARY KEY NOT NULL,
    name varchar(255) NOT NULL,
    dish_type dish_type_enum
);