package org.antonio.Entity.model.order;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Order {
  private int id;
  private String reference;
  private Instant creationDatetime;
  private List<DishOrder> dishOrders;
  private Payment_status paymentStatus;

  // method to get total of amount : value added tax
  public Double getTotalAmountWithoutVAT () {
    return dishOrders.stream()
        .mapToDouble(dishOrder -> dishOrder.getDish().getPrice() * dishOrder.getQuantity())
        .sum();
  }

  // method to het total of amount without VAT
  public double getTotalAmountWithVAT () {
    return getTotalAmountWithoutVAT() * (1.2);
  }
}
