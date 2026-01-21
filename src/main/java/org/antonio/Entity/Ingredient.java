package org.antonio.Entity;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ingredient {
  private Integer id;
  private String name;
  private Double price;
  private CategoryEnum category;
  private Dish dish;
  private UnitEnum unit;
  private Double requiredQuantity;

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
