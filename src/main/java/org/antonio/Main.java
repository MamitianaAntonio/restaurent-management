package org.antonio;

import org.antonio.Entity.DataRetriever;
import org.antonio.Entity.Dish;
import org.antonio.Entity.DishTypeEnum;
import org.antonio.Entity.Ingredient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    try {
      Dish salade = dataRetriever.findDishById(1);
      System.out.println("Name of the dish : " + salade.getName());

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
      List<Ingredient> ingredients = new ArrayList<>();
      Ingredient laitue = new Ingredient();
      laitue.setId(1);
      Ingredient tomate = new Ingredient();
      tomate.setId(2);
      ingredients.add(laitue);
      ingredients.add(tomate);

      Dish savedDish = dataRetriever.saveDish(newSalade);
      System.out.println("Saved dish : " + savedDish.getName() + " with ID " + savedDish.getId());

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}