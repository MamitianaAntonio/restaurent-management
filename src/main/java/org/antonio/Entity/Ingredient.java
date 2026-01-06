package org.antonio.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Ingredient {
  private int id;
  private String name;
  private Double price;
  private CategoryEnum category;
  private Dish dish;

  public Ingredient(int id, String name, Double price, CategoryEnum category, Dish dish) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
    this.dish = dish;
  }

  // get dish name
  public String getDishName () {
    return dish.getName();
  }
}
