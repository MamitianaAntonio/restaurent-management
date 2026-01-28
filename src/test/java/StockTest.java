import org.antonio.Entity.model.ingredient.CategoryEnum;
import org.antonio.Entity.model.ingredient.Ingredient;
import org.antonio.Entity.model.ingredient.UnitEnum;
import org.antonio.Entity.model.stock.MovementTypeEnum;
import org.antonio.Entity.model.stock.StockMovement;
import org.antonio.Entity.model.stock.StockValue;
import org.antonio.Entity.service.DataRetriever;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class StockTest {
  DataRetriever dataRetriever = new DataRetriever();

  @Test
  public void testFindIngredientsByIdWithStockMovement () throws SQLException {
    Ingredient laitue = dataRetriever.findIngredientByIdWithStockMovements(1);
    Ingredient tomate = dataRetriever.findIngredientByIdWithStockMovements(2);
    Ingredient poulet = dataRetriever.findIngredientByIdWithStockMovements(3);
    Ingredient chocolat = dataRetriever.findIngredientByIdWithStockMovements(4);
    Ingredient beurre = dataRetriever.findIngredientByIdWithStockMovements(5);

    Instant instantNow = Instant.now();

    StockValue laitueStock = laitue.getStockValueAt(instantNow);
    Assertions.assertEquals(4.8, laitueStock.getQuantity(), 0.1);
    Assertions.assertEquals(UnitEnum.KG, laitueStock.getUnit());

    StockValue tomateStock = tomate.getStockValueAt(instantNow);
    Assertions.assertEquals(3.85, tomateStock.getQuantity(), 0.01);
    Assertions.assertEquals(UnitEnum.KG, tomateStock.getUnit());

    StockValue pouletStock = poulet.getStockValueAt(instantNow);
    Assertions.assertEquals(9.0, pouletStock.getQuantity(), 0.01);
    Assertions.assertEquals(UnitEnum.KG, pouletStock.getUnit());

    StockValue chocolatStock = chocolat.getStockValueAt(instantNow);
    Assertions.assertEquals(2.7, chocolatStock.getQuantity(), 0.01);
    Assertions.assertEquals(UnitEnum.KG, chocolatStock.getUnit());

    StockValue beurreStock = beurre.getStockValueAt(instantNow);
    Assertions.assertEquals(2.3, beurreStock.getQuantity(), 0.01);
    Assertions.assertEquals(UnitEnum.KG, beurreStock.getUnit());
  }

  @Test
  public void testGetStockValueAt_20240106_1200() throws SQLException {
    Instant targetTime = Instant.parse("2024-01-06T12:00:00Z");

    Ingredient laitue = dataRetriever.findIngredientByIdWithStockMovements(1);
    Assertions.assertEquals(4.8, laitue.getStockValueAt(targetTime).getQuantity(), 0.01);

    Ingredient tomate = dataRetriever.findIngredientByIdWithStockMovements(2);
    Assertions.assertEquals(3.85, tomate.getStockValueAt(targetTime).getQuantity(), 0.01);

    Ingredient poulet = dataRetriever.findIngredientByIdWithStockMovements(3);
    Assertions.assertEquals(10.0, poulet.getStockValueAt(targetTime).getQuantity(), 0.01);

    Ingredient chocolat = dataRetriever.findIngredientByIdWithStockMovements(4);
    Assertions.assertEquals(3.0, chocolat.getStockValueAt(targetTime).getQuantity(), 0.01);

    Ingredient beurre = dataRetriever.findIngredientByIdWithStockMovements(5);
    Assertions.assertEquals(2.5, beurre.getStockValueAt(targetTime).getQuantity(), 0.01);
  }

  @Test
  public void testSaveIngredient_OnConflictDoNothing() throws SQLException {
    Ingredient laitue = new Ingredient();
    laitue.setId(1);
    laitue.setName("Laitue");
    laitue.setPrice(800.0);
    laitue.setCategory(CategoryEnum.VEGETABLE);

    StockMovement existingMovement = new StockMovement();
    existingMovement.setId(2);
    existingMovement.setValue(new StockValue(999.0, UnitEnum.KG));
    existingMovement.setType(MovementTypeEnum.OUT);
    existingMovement.setCreationDatetime(Instant.parse("2024-01-06T12:00:00Z"));

    laitue.setStockMovementList(List.of(existingMovement));
    dataRetriever.saveIngredient(laitue);

    Ingredient retrieved = dataRetriever.findIngredientByIdWithStockMovements(1);
    StockMovement movement2 = retrieved.getStockMovementList().stream()
        .filter(m -> m.getId() == 2)
        .findFirst()
        .orElse(null);

    Assertions.assertNotNull(movement2);
    Assertions.assertEquals(0.2, movement2.getValue().getQuantity(), 0.01);
  }

  @Test
  public void testSaveIngredient_AddNewMovement() throws SQLException {
    Ingredient laitue = new Ingredient();
    laitue.setId(1);
    laitue.setName("Laitue");
    laitue.setPrice(800.0);
    laitue.setCategory(CategoryEnum.VEGETABLE);

    StockMovement newMovement = new StockMovement();
    newMovement.setId(11);
    newMovement.setValue(new StockValue(5.0, UnitEnum.KG));
    newMovement.setType(MovementTypeEnum.IN);
    newMovement.setCreationDatetime(Instant.now());

    laitue.setStockMovementList(List.of(newMovement));
    dataRetriever.saveIngredient(laitue);

    Ingredient retrieved = dataRetriever.findIngredientByIdWithStockMovements(1);
    Assertions.assertTrue(retrieved.getStockMovementList().stream()
        .anyMatch(m -> m.getId() == 11));
  }
}
