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
}
