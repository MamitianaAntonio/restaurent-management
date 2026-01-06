package org.antonio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Ingredient {
  private int id;
  private String name;
  private Double price;
  private CategoryEnum category;
  private Dish dish;

  // constructor for the test
  public Ingredient(String name, CategoryEnum category, Double price) {
    this.name = name;
    this.category = category;
    this.price = price;
  }

  // get dish name
  public String getDishName () {
    return dish.getName();
  }
}
