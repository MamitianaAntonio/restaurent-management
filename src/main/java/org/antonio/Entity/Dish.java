package org.antonio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
  private Integer id;
  private String name;
  private DishTypeEnum dishType;
  private List<Ingredient> ingredients;

  public Dish (int id, String name, DishTypeEnum dishType) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
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

  // new method
  public double getDishCost () {
    return ingredients.stream()
        .mapToDouble(ingredient -> {
          if (ingredient.getRequiredQuantity() == null) {
            throw new IllegalStateException("It is impossible to calculate the cost of ingredient : " + ingredient.getName());
          }
          return ingredient.getPrice() * ingredient.getRequiredQuantity();
        })
        .sum();
  }
}
