package org.antonio.Entity.model.order;

import lombok.*;
import org.antonio.Entity.model.dish.Dish;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DishOrder {
  private int id;
  private Dish dish;
  private Integer quantity;
}
