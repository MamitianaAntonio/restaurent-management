package org.antonio.Entity.model.dish;

import lombok.*;
import org.antonio.Entity.model.ingredient.Ingredient;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Dish {
  private Integer id;
  private String name;
  private Double price;
  private DishTypeEnum dishType;
  private List<Ingredient> ingredients;

  public Dish (int id, String name, DishTypeEnum dishType) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.ingredients = new ArrayList<>();
  }

  public Dish() {
    this.ingredients = new ArrayList<>();
  }

  // get dish price methods
  public double getDishPrice() {
    double total = 0;

    for (Ingredient ingredient : ingredients) {
      total += ingredient.getPrice();
    }

    return total;
  }

  // new get dish cost
  public double getDishCost () {
    return ingredients.stream()
        .mapToDouble(ingredient -> {
          if (ingredient.getRequiredQuantity() == null) {
            throw new IllegalStateException("It is impossible to calculate the cost of ingredient (the quantity is unknown ) : "
                + ingredient.getName());
          }
          return ingredient.getPrice() * ingredient.getRequiredQuantity();
        })
        .sum();
  }

  // get cross margin method
  public double getCrossMargin () {
    if (this.price == null) {
      throw new RuntimeException("Dish does not have price .");
    }

    return this.price - getDishCost();
  }
}
