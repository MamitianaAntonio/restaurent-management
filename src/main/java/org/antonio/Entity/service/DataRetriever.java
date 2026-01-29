package org.antonio.Entity.service;

import org.antonio.Entity.db.DBConnection;
import org.antonio.Entity.model.dish.Dish;
import org.antonio.Entity.model.dish.DishTypeEnum;
import org.antonio.Entity.model.ingredient.CategoryEnum;
import org.antonio.Entity.model.ingredient.Ingredient;
import org.antonio.Entity.model.ingredient.UnitEnum;
import org.antonio.Entity.model.order.DishOrder;
import org.antonio.Entity.model.order.Order;
import org.antonio.Entity.model.stock.MovementTypeEnum;
import org.antonio.Entity.model.stock.StockMovement;
import org.antonio.Entity.model.stock.StockValue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
  // new findDishById
  public Dish findDishById(Integer id) throws SQLException {
    Dish dish = null;
    List<Ingredient> ingredients = new ArrayList<>();

    String sqlQuery = """
    SELECT d.id AS dish_id, d.name AS dish_name, d.dish_type, d.price AS dish_price,
           i.id AS ingredient_id, i.name AS ingredient_name,
           i.price AS ingredient_price, i.category,
           di.quantity_required, di.unit
    FROM Dish d
    LEFT JOIN DishIngredient di ON d.id = di.id_dish
    LEFT JOIN Ingredient i ON di.id_ingredient = i.id
    WHERE d.id = ?
  """;

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        if (dish == null) {
          dish = new Dish();
          dish.setId(rs.getInt("dish_id"));
          dish.setName(rs.getString("dish_name"));
          dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
          dish.setPrice(rs.getDouble("dish_price"));
          dish.setIngredients(ingredients);
        }

        if (rs.getInt("ingredient_id") != 0) {
          Ingredient ingredient = new Ingredient();
          ingredient.setId(rs.getInt("ingredient_id"));
          ingredient.setName(rs.getString("ingredient_name"));
          ingredient.setPrice(rs.getDouble("ingredient_price"));
          ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

          double quantity = rs.getDouble("quantity_required");
          if (!rs.wasNull()) {
            ingredient.setRequiredQuantity(quantity);
            ingredient.setUnit(UnitEnum.valueOf(rs.getString("unit")));
          }

          ingredients.add(ingredient);
        }
      }

      connection.close();
    }

    return dish;
  }

  // find ingredients using pagination
  public List<Ingredient> findIngredients (int page, int size) throws SQLException {
    List<Ingredient> ingredients = new ArrayList<>();
    int offset = (page - 1) * size;
    String sqlQuery = "SELECT * FROM ingredient ORDER BY id LIMIT ? OFFSET ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sqlQuery)){
      statement.setInt(1, size);
      statement.setInt(2, offset);

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          Ingredient ingredient = new Ingredient();
          ingredient.setId(rs.getInt("id"));
          ingredient.setName(rs.getString("name"));
          ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
          ingredients.add(ingredient);
        }
      }
    }

    return ingredients;
  }

  // verify if an ingredient exist
  private boolean existsIngredient(Connection conn, String name) throws SQLException {
    String sqlQuery = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
      stmt.setString(1, name);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  // method to create new ingredient with principle of atomicity
  public List<Ingredient> createIngredients (List<Ingredient> newIngredient) throws SQLException{
    List<Ingredient> createdIngredients = new ArrayList<>();
    String sqlQuery = "INSERT INTO ingredient (name, category, price) VALUES (?, ?, ?)";

    try(Connection connection = DBConnection.getConnection()){
      connection.setAutoCommit(false);

      try (PreparedStatement statement = connection.prepareStatement(sqlQuery)){
        for (Ingredient ingredient : newIngredient) {
          if (existsIngredient(connection, ingredient.getName())) {
            throw new RuntimeException("Ingredient : " + ingredient.getName() + "  already exist");
          }

          statement.setString(1, ingredient.getName());
          statement.setObject(2, ingredient.getCategory(), Types.OTHER);
          statement.setDouble(3, ingredient.getPrice());
          statement.executeUpdate();
        }
        connection.commit();
      }
    }

    return newIngredient;
  }

  // methods to save dish or update if it already exists
  public Dish saveDish (Dish dishToSave) throws SQLException {
    String upsertDishSql = """
      INSERT INTO Dish (id, name, dish_type, price)
      VALUES (?, ?, ?::dish_type_enum, ?)
      ON CONFLICT (id) DO UPDATE
      SET name = EXCLUDED.name,
        dish_type = EXCLUDED.dish_type,
        price = EXCLUDED.price
      RETURNING id, name, dish_type, price
    """;

    Connection connection = null;
    try {
      connection = DBConnection.getConnection();
      connection.setAutoCommit(false);
      Integer dishId;

      try (PreparedStatement statement = connection.prepareStatement(upsertDishSql)) {
        if (dishToSave.getId() != null) {
          statement.setInt(1, dishToSave.getId());
        } else {
          statement.setInt(1, getNextSerialValue(connection, "dish", "id"));
        }
        statement.setString(2, dishToSave.getName());
        statement.setString(3, dishToSave.getDishType().name());

        if(dishToSave.getPrice() != null) {
          statement.setDouble(4, dishToSave.getPrice());
        } else {
          statement.setNull(4, Types.DOUBLE);
        }

        try (ResultSet rs = statement.executeQuery()) {
          rs.next();
          dishId = rs.getInt(1);
        }
      }

      List<Ingredient> newIngredients = dishToSave.getIngredients();
      System.out.println("Ingredients avant attach: " + (newIngredients != null ? newIngredients.size() : "null"));

      if (newIngredients != null) {
        detachIngredients(connection, dishId, newIngredients);
        attachIngredients(connection, dishId, newIngredients);
      }

      connection.commit();
      return findDishById(dishId);

    } catch (SQLException e) {
      if (connection != null) {
        connection.rollback();
      }
      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  // method to detach ingredient
  private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
      throws SQLException {

    String deleteSql = """
                    DELETE FROM DishIngredient
                    WHERE id_dish = ?
                """;

    try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
      ps.setInt(1, dishId);
      ps.executeUpdate();
    }
  }

  // method to attach ingredient on a dish
  private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
      throws SQLException {
    if (ingredients == null || ingredients.isEmpty()) {
      return;
    }

    String attachSql = """
        INSERT INTO DishIngredient (id_dish, id_ingredient)
        VALUES (?, ?)
        ON CONFLICT DO NOTHING
    """;

    try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
      for (Ingredient ingredient : ingredients) {
        if (ingredient.getId() == null) {
          throw new SQLException("Ingredient must have an id to be attached");
        }
        ps.setInt(1, dishId);
        ps.setInt(2, ingredient.getId());
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  // method to get serial sequence name
  private String getSerialSequenceName(Connection conn, String tableName, String columnName)
      throws SQLException {

    String sql = "SELECT pg_get_serial_sequence(?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tableName);
      ps.setString(2, columnName);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    }
    return null;
  }

  // nethod to get next serial value on db
  private int getNextSerialValue(Connection conn, String tableName, String columnName)
      throws SQLException {

    String sequenceName = getSerialSequenceName(conn, tableName, columnName);
    if (sequenceName == null) {
      throw new IllegalArgumentException(
          "Any sequence found for " + tableName + "." + columnName
      );
    }
    updateSequenceNextValue(conn, tableName, columnName, sequenceName);

    String nextValSql = "SELECT nextval(?)";

    try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
      ps.setString(1, sequenceName);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  // methods to update the sequence value on db
  private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
    String setValSql = String.format(
        "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
        sequenceName, columnName, tableName
    );

    try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
      ps.executeQuery();
    }
  }

  public List<Dish> findDishesByIngredientName(String ingredientName) throws SQLException {
    List<Dish> dishes = new ArrayList<>();

    String sqlQuery = """
      SELECT DISTINCT d.id, d.name, d.dish_type, d.price
      FROM Dish d
      INNER JOIN DishIngredient di ON d.id = di.id_dish
      INNER JOIN Ingredient i ON di.id_ingredient = i.id
      WHERE i.name ILIKE ?
    """;

    try (Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
      statement.setString(1, "%" + ingredientName + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setPrice(rs.getDouble("price"));
        dishes.add(dish);
      }
    }
    return dishes;
  }

  // method to find ingredients by criteria
  public List<Ingredient> findIngredientsByCriteria(
      String ingredientName,
      CategoryEnum category,
      String dishName,
      int page,
      int size
  ) throws SQLException {
    List<Ingredient> ingredients = new ArrayList<>();
    StringBuilder sqlQuery = new StringBuilder("""
      SELECT DISTINCT i.id, i.name, i.price, i.category,
             d.id AS dish_id, d.name AS dish_name, d.dish_type
      FROM Ingredient i
      LEFT JOIN DishIngredient di ON i.id = di.id_ingredient
      LEFT JOIN Dish d ON di.id_dish = d.id
      WHERE 1=1
  """);

    if (ingredientName != null && !ingredientName.isEmpty()) {
      sqlQuery.append(" AND i.name ILIKE ?");
    }
    if (category != null) {
      sqlQuery.append(" AND i.category = ?::category_enum");
    }
    if (dishName != null && !dishName.isEmpty()) {
      sqlQuery.append(" AND d.name ILIKE ?");
    }

    sqlQuery.append(" ORDER BY i.id ASC LIMIT ? OFFSET ?");

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sqlQuery.toString())) {
      int index = 1;
      if (ingredientName != null && !ingredientName.isEmpty()) {
        statement.setString(index++, "%" + ingredientName + "%");
      }
      if (category != null) {
        statement.setString(index++, category.name());
      }
      if (dishName != null && !dishName.isEmpty()) {
        statement.setString(index++, "%" + dishName + "%");
      }

      statement.setInt(index++, size);
      statement.setInt(index, (page - 1) * size);

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

        int idDish = rs.getInt("dish_id");
        if (!rs.wasNull()) {
          Dish dish = new Dish();
          dish.setId(idDish);
          dish.setName(rs.getString("dish_name"));
          dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
          ingredient.setDish(dish);
        } else {
          ingredient.setDish(null);
        }
        ingredients.add(ingredient);
      }
    }
    return ingredients;
  }

  public List<Ingredient> findIngredientByDishId(Integer idDish) throws SQLException{
    List<Ingredient> ingredients = new ArrayList<>();

    String sqlQuery = """
        SELECT i.id, i.name, i.price, i.category, di.quantity_required, di.unit
        FROM ingredient i
        INNER JOIN DishIngredient di ON i.id = di.id_ingredient
        WHERE di.id_dish = ?
     """;

    try (Connection connection = DBConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
      statement.setInt(1, idDish);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

        double quantity = rs.getDouble("quantity_required");
        if(!rs.wasNull()) {
          ingredient.setRequiredQuantity(quantity);
          ingredient.setUnit(UnitEnum.valueOf(rs.getString("unit")));
        }
        ingredients.add(ingredient);
      }
    }

    return ingredients;
  }

  // method to save ingredient including stock movement
  public Ingredient saveIngredient (Ingredient toSave) throws SQLException {
    String ingredientSql = """
      INSERT INTO ingredient (id, name, price, category) 
            VALUES (?, ?, ?, ?::category_enum)
            on conflict (id) do update
                set name = excluded.name,
                    price = excluded.price,
                    category = excluded.category::category_enum
            returning id, name, price, category; 
    """;

    String stockSql = """
      INSERT INTO StockMovement (id, id_ingredient, quantity, unit, type, creation_datetime)
          VALUES (?, ?, ?, ?::unit_type, ?::movement_type, ?)
          ON CONFLICT (id) DO NOTHING;
    """;

    Connection connection = null;
    try {
       connection = DBConnection.getConnection();
       PreparedStatement ingredientStatement = connection.prepareStatement(ingredientSql);
       ingredientStatement.setInt(1, toSave.getId());
       ingredientStatement.setString(2, toSave.getName());
       ingredientStatement.setDouble(3, toSave.getPrice());
       ingredientStatement.setString(4, toSave.getCategory().toString());

       ResultSet ingredientResultSet = ingredientStatement.executeQuery();
       Ingredient savedIngredient = null;
       if (ingredientResultSet.next()) {
         savedIngredient = mapToIngredient(ingredientResultSet);
         savedIngredient.setStockMovementList(toSave.getStockMovementList());
       }

       PreparedStatement stockStatement = connection.prepareStatement(stockSql);
       if (toSave.getStockMovementList() != null) {
         for (StockMovement movement : toSave.getStockMovementList()) {
           stockStatement.setInt(1, movement.getId());
           stockStatement.setInt(2, savedIngredient.getId());
           stockStatement.setDouble(3, movement.getValue().getQuantity());
           stockStatement.setString(4, movement.getValue().getUnit().toString());
           stockStatement.setString(5, movement.getType().toString());
           stockStatement.setTimestamp(6, Timestamp.from(movement.getCreationDatetime()));
           stockStatement.addBatch();
         }
         stockStatement.executeBatch();
       }

       connection.setAutoCommit(false);
       connection.commit();
       return savedIngredient;
    } catch (SQLException e) {
      if (connection != null) {
        connection.rollback();
      }
      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  private Ingredient mapToIngredient (ResultSet rs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(rs.getInt("id"));
    ingredient.setName(rs.getString("name"));
    ingredient.setPrice(rs.getDouble("price"));
    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
    ingredient.setStockMovementList(new ArrayList<>());
    return ingredient;
  }

  // method to get or find ingredient id with stock movement
  public Ingredient findIngredientByIdWithStockMovements (Integer id) throws SQLException {
    String sqlQuery = """
      SELECT i.id, i.name, i.price, i.category,
             sm.id AS movement_id, sm.quantity, sm.unit, sm.type, sm.creation_datetime
      FROM Ingredient i
      LEFT JOIN StockMovement sm ON i.id = sm.id_ingredient
      WHERE i.id = ?
      ORDER BY sm.creation_datetime ASC
  	""";

    Connection connection = null;
    try {
      connection = DBConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sqlQuery);

      statement.setInt(1, id);
      ResultSet resultSet = statement.executeQuery();

      Ingredient ingredient = null;
      List<StockMovement> movements = new ArrayList<>();

      while(resultSet.next()) {
        if (ingredient == null) {
          ingredient = new Ingredient();
          ingredient.setId(resultSet.getInt("id"));
          ingredient.setName(resultSet.getString("name"));
          ingredient.setPrice(resultSet.getDouble("price"));
          ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
        }

        int movementId = resultSet.getInt("movement_id");
        if (!resultSet.wasNull()) {
          StockMovement movement = new StockMovement();
          movement.setId(movementId);

          StockValue value = new StockValue();
          value.setQuantity(resultSet.getDouble("quantity"));
          value.setUnit(UnitEnum.valueOf(resultSet.getString("unit")));
          movement.setValue(value);

          movement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
          movement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

          movements.add(movement);
        }
      }

      if (ingredient != null) {
        ingredient.setStockMovementList(movements);
      }
      return ingredient;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Order mapToOrder (ResultSet rs) throws SQLException {
   Order order = new Order();
   order.setId(rs.getInt("id"));
   order.setReference(rs.getString("reference"));
   order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
   return order;
  }

  private DishOrder mapTodishOrder (ResultSet rs) throws SQLException{
    DishOrder dishOrder = new DishOrder();
    dishOrder.setId(rs.getInt("id"));
    dishOrder.setDish(findDishById(rs.getInt("id_dish")));
    dishOrder.setQuantity(rs.getInt("quantity"));
    return dishOrder;
  }

  // find stock movements by ingredient id
  public List<StockMovement> findStockMovementsByIngredientId(Integer id) throws SQLException {
    Connection connection = DBConnection.getConnection();
    List<StockMovement> stockMovementList = new ArrayList<>();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement("""
        SELECT id, quantity, unit, type, creation_datetime
        FROM StockMovement
        WHERE StockMovement.id_ingredient = ?;
      """);
      preparedStatement.setInt(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(resultSet.getInt("id"));
        stockMovement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
        stockMovement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

        StockValue stockValue = new StockValue();
        stockValue.setQuantity(resultSet.getDouble("quantity"));
        stockValue.setUnit(UnitEnum.valueOf(resultSet.getString("unit")));
        stockMovement.setValue(stockValue);

        stockMovementList.add(stockMovement);
      }
      DBConnection.closeConnection(connection);
      return stockMovementList;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
