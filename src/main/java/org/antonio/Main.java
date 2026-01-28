package org.antonio;

import org.antonio.Entity.service.DataRetriever;
import org.antonio.Entity.model.dish.Dish;
import org.antonio.Entity.model.dish.DishTypeEnum;
import org.antonio.Entity.model.ingredient.Ingredient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    try {
      Dish salade = dataRetriever.findDishById(1);
      System.out.println(salade);

      try {
        double costSalade = salade.getDishCost();
        System.out.println("Coût de la salade : " + costSalade);
      } catch (IllegalStateException e) {
        System.out.println("Ingredient cost is null  : " + e.getMessage());
      }

      System.out.println();
      System.out.println("Dish creation");
      Dish newSalade = new Dish();
      newSalade.setName("Special salade");
      newSalade.setDishType(DishTypeEnum.START);
      newSalade.setPrice(500.0);
      List<Ingredient> ingredients = new ArrayList<>();
      Ingredient laitue = new Ingredient();
      laitue.setId(1);
      Ingredient tomate = new Ingredient();
      tomate.setId(1);
      ingredients.add(laitue);
      ingredients.add(tomate);
      newSalade.setIngredients(ingredients);

      Dish savedDish = dataRetriever.saveDish(newSalade);
      System.out.println(savedDish.getIngredients());
      System.out.println("Saved dish : " + savedDish.getName() + " with ID " + savedDish.getId() + " and price : " + savedDish.getPrice());

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}