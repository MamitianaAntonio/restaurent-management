-- create new user
CREATE USER mini_dish_db_manager;
-- associate user to the database
CREATE DATABASE mini_dish_db WITH OWNER = mini_dish_db_manager ENCODING = 'UTF8';
-- all privileges
GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_db_manager;
-- add privileges to create
GRANT CREATE ON DATABASE mini_dish_db TO mini_dish_db_manager;
-- connection
\c mini_dish_db;