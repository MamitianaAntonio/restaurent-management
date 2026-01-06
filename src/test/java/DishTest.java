import org.antonio.Entity.DataRetriever;
import org.antonio.Entity.Dish;
import org.antonio.Entity.DishTypeEnum;
import org.junit.Test;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DishTest {
  private DataRetriever dataRetriever = new DataRetriever();

  @Test
  public void findDishById_shouldReturnSaladeFraicheWithTwoIngredients() throws SQLException {
    Integer id = 1;

    Dish dish = dataRetriever.findDishById(id);

    assertNotNull(dish);
    assertEquals("Salade fraîche", dish.getName());
    assertEquals(DishTypeEnum.START, dish.getDishType());
    assertNotNull(dish.getIngredients());
    assertEquals(2, dish.getIngredients().size());
    assertEquals("Laitue", dish.getIngredients().get(0).getName());
    assertEquals("Tomate", dish.getIngredients().get(1).getName());
  }
}
