package org.antonio.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
