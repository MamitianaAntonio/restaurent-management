import org.antonio.Entity.DataRetriever;
import org.antonio.Entity.Ingredient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.List;

public class IngredientTest {
  private DataRetriever dataRetriever = new DataRetriever();

  @Test
  public void findIngredient_page2size2_shouldReturnPouletAndChocolat () throws SQLException {
    int page = 2;
    int size = 2;

    List<Ingredient> ingredients = dataRetriever.findIngredients(page, size);

    Assertions.assertNotNull(ingredients);
    Assertions.assertEquals(2, ingredients.size());
    Assertions.assertEquals("Poulet", ingredients.get(0).getName());
    Assertions.assertEquals("Chocolat", ingredients.get(1).getName());
  }

  @Test
  public void findIngredients_page3Size5_shouldReturnEmptyList() throws SQLException {
    int page = 3;
    int size = 5;

    List<Ingredient> ingredients = dataRetriever.findIngredients(page, size);

    Assertions.assertNotNull(ingredients);
    Assertions.assertTrue(ingredients.isEmpty());
  }
}
