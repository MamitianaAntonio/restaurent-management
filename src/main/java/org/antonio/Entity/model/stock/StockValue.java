package org.antonio.Entity.model.stock;

import lombok.*;
import org.antonio.Entity.model.ingredient.UnitEnum;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StockValue {
  private Double quantity;
  private UnitEnum unit;
}
