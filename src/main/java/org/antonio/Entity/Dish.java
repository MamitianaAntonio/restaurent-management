package org.antonio.Entity;

import java.util.ArrayList;
import java.util.List;

public class Dish {
  private int id;
  private String name;
  private DishTypeEnum dishType;
  private List<Ingredient> ingredients;

  public Dish (int id, String name, DishTypeEnum dishType) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.ingredients = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DishTypeEnum getDishType() {
    return dishType;
  }

  public void setDishType(DishTypeEnum dishType) {
    this.dishType = dishType;
  }

  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  public void setIngredients(List<Ingredient> ingredients) {
    this.ingredients = ingredients;
  }

  // get dish price methods
  public double getDishPrice() {
    double total = 0;

    for (Ingredient ingredient : ingredients) {
      total += ingredient.getPrice();
    }

    return total;
  }
}
