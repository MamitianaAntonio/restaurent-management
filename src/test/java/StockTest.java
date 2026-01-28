import org.antonio.Entity.model.ingredient.Ingredient;
import org.antonio.Entity.model.ingredient.UnitEnum;
import org.antonio.Entity.model.stock.StockValue;
import org.antonio.Entity.service.DataRetriever;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.time.Instant;

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
}
