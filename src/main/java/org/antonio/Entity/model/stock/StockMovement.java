package org.antonio.Entity.model.stock;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class StockMovement {
  private Integer id;
  private StockValue value;
  private MovementTypeEnum type;
  private Instant creationDatetime;
}
