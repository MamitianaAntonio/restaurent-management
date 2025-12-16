package org.antonio.Entity;

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

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public CategoryEnum getCategory() {
    return category;
  }

  public void setCategory(CategoryEnum category) {
    this.category = category;
  }

  public Dish getDish() {
    return dish;
  }

  public void setDish(Dish dish) {
    this.dish = dish;
  }
}
