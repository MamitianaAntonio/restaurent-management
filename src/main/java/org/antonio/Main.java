package org.antonio;

import org.antonio.Entity.model.order.Order;
import org.antonio.Entity.model.order.PaymentStatus;
import org.antonio.Entity.model.sale.Sale;
import org.antonio.Entity.service.DataRetriever;
import org.antonio.Entity.model.dish.Dish;
import org.antonio.Entity.model.dish.DishTypeEnum;
import org.antonio.Entity.model.ingredient.Ingredient;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dr = new DataRetriever();

    try {
      // Vérifie que findOrderByReference marche
      System.out.print("Testing findOrderByReference... ");
      Order order = dr.findOrderByReference("ORD00001");
      System.out.println("OK - Found order: " + order.getReference());

      // Vérifie que createSaleFrom existe
      System.out.print("Testing createSaleFrom method exists... ");
      try {
        // Juste pour vérifier que la méthode existe
        dr.getClass().getMethod("createSaleFrom", Order.class);
        System.out.println("OK - Method exists");

        // Tester rapidement
        System.out.println("\nQuick test of createSaleFrom:");
        System.out.println("Order status: " + order.getPaymentStatus());

        try {
          Sale sale = dr.createSaleFrom(order);
          System.out.println("Result: Created sale ID " + sale.getId());
        } catch (RuntimeException e) {
          System.out.println("Result: " + e.getMessage());
        }

      } catch (NoSuchMethodException e) {
        System.out.println("ERROR: Method doesn't exist!");
      }

    } catch (Exception e) {
      System.out.println("ERROR: " + e.getMessage());
    }
  }
}