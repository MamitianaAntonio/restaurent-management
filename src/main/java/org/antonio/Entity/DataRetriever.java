package org.antonio.Entity;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
  // method to find dish by id
  public Dish findDishById(Integer id) throws SQLException {
    Dish dish = null;
    List<Ingredient> ingredients = new ArrayList<>();

    String sqlQuery = """
      SELECT d.id AS dish_id, d.name AS dish_name, d.dish_type,
             i.id AS ingredient_id, i.name AS ingredient_name,
             i.price, i.category
      FROM Dish d
      LEFT JOIN Ingredient i ON d.id = i.id_dish
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
          dish.setIngredients(ingredients);
        }

        // add to ingredients if there is ingredient
        if (rs.getInt("ingredient_id") != 0) {
          Ingredient ingredient = new Ingredient();
          ingredient.setId(rs.getInt("ingredient_id"));
          ingredient.setName(rs.getString("ingredient_name"));
          ingredient.setPrice(rs.getDouble("price"));
          ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
          ingredients.add(ingredient);
        }
      }
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
    String insertDishSql = """
        INSERT INTO dish (name, dish_type)
        VALUES (?, ?)
        RETURNING id
    """;

    String updateDishSql = """
        UPDATE dish
        SET name = ?, dish_type = ?
        WHERE id = ?
    """;

    String existsLinkSql = """
        SELECT 1 FROM ingredient
        WHERE id = ? AND id_dish = ?
    """;

    String linkIngredientSql = """
        UPDATE ingredient
        SET id_dish = ?
        WHERE id = ?
    """;

    try (Connection connection = DBConnection.getConnection()) {
      connection.setAutoCommit(false);

      if (dishToSave.getId() == null) {
        try (PreparedStatement statement = connection.prepareStatement(insertDishSql)) {
          statement.setString(1, dishToSave.getName());
          statement.setObject(2, dishToSave.getDishType(), Types.OTHER);

          ResultSet rs = statement.executeQuery();
          if (rs.next()) {
            dishToSave.setId(rs.getInt("id"));
          }
        }
      } else {
        try (PreparedStatement statement = connection.prepareStatement(updateDishSql)) {
          statement.setString(1, dishToSave.getName());
          statement.setObject(2, dishToSave.getDishType(), Types.OTHER);
          statement.setInt(3, dishToSave.getId());
          statement.executeUpdate();
        }
      }

      for (Ingredient ingredient : dishToSave.getIngredients()) {
        boolean alreadyLinked;

        try (PreparedStatement checkStatement = connection.prepareStatement(existsLinkSql)) {
          checkStatement.setInt(1, ingredient.getId());
          checkStatement.setInt(2, dishToSave.getId());

          ResultSet rs = checkStatement.executeQuery();
          alreadyLinked = rs.next();
        }

        if (!alreadyLinked) {
          try (PreparedStatement linkStatement = connection.prepareStatement(linkIngredientSql)) {
            linkStatement.setInt(1, dishToSave.getId());
            linkStatement.setInt(2, ingredient.getId());
            linkStatement.executeUpdate();
          }
        }
      }
      connection.commit();
      return dishToSave;
    }
  }

  public List<Dish> findDishesByIngredientName(String ingredientName) throws SQLException {
    List<Dish> dishes = new ArrayList<>();
    String sql = """
        SELECT DISTINCT d.id, d.name, d.dish_type
        FROM dish d
        JOIN ingredient i ON i.id_dish = d.id
        WHERE i.name ILIKE ?
    """;

    try (Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, "%" + ingredientName + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
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
}
