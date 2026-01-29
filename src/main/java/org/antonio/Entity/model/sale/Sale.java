package org.antonio.Entity.model.sale;

import lombok.*;
import org.antonio.Entity.model.order.Order;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Sale {
  private int id;
  private Instant saleDateTime;
  private Order order;
}
