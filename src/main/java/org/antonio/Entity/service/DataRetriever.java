package org.antonio.Entity.service;

import org.antonio.Entity.db.DBConnection;
import org.antonio.Entity.model.*;

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
    StringBuilder sql = new StringBuilder(
        "SELECT i.id, i.name, i.price, i.category, i.id_dish, " +
            "d.name AS dish_name, d.dish_type " +
            "FROM ingredient i " +
            "LEFT JOIN dish d ON i.id_dish = d.id " +
            "WHERE 1=1"
    );

    if (ingredientName != null && !ingredientName.isEmpty()) {
      sql.append(" AND i.name ILIKE ?");
    }
    if (category != null) {
      sql.append(" AND i.category = ?");
    }
    if (dishName != null && !dishName.isEmpty()) {
      sql.append(" AND d.name ILIKE ?");
    }

    sql.append(" ORDER BY i.id ASC LIMIT ? OFFSET ?");

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql.toString())) {
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

        int idDish = rs.getInt("id_dish");
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
}
