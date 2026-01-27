package org.antonio.Entity.model.ingredient;

import lombok.*;
import org.antonio.Entity.model.dish.Dish;
import org.antonio.Entity.model.stock.MovementTypeEnum;
import org.antonio.Entity.model.stock.StockMovement;
import org.antonio.Entity.model.stock.StockValue;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Ingredient {
  private Integer id;
  private String name;
  private Double price;
  private CategoryEnum category;
  private Dish dish;
  private UnitEnum unit;
  private Double requiredQuantity;
  private List<StockMovement> stockMovementList;

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

  // method to return a stock at a certain instant
  public StockValue getStockValueAt (Instant timeTarget) {
    List<StockMovement> sortedMovement = stockMovementList.stream()
        .sorted(Comparator.comparing(StockMovement::getCreationDatetime))
        .toList();

    StockValue stockAtTime = new StockValue();
    stockAtTime.setUnit(sortedMovement.get(0).getValue().getUnit());
    double totalQuantity = 0;

    for (StockMovement movement : sortedMovement) {
      if(!movement.getCreationDatetime().isAfter(timeTarget)) {
        if (movement.getType() == MovementTypeEnum.IN) {
          totalQuantity += movement.getValue().getQuantity();
        } else if (movement.getType() == MovementTypeEnum.OUT) {
          totalQuantity -= movement.getValue().getQuantity();
        }
      }
    }

    stockAtTime.setQuantity(totalQuantity);
    return stockAtTime;
  }
}
