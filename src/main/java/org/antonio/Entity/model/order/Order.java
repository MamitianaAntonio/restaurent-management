package org.antonio.Entity.model.order;

import java.time.Instant;
import java.util.List;

public class Order {
  private int id;
  private String reference;
  private Instant creationDatetime;
  private List<DishOrder> dishOrders;
}
