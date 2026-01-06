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

  // get dish name
  public String getDishName () {
    return dish.getName();
  }
}
