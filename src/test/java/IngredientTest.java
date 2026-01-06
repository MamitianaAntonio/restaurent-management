import org.antonio.Entity.CategoryEnum;
import org.antonio.Entity.DataRetriever;
import org.antonio.Entity.Ingredient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
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

  @Test
  public void createIngredients_shouldReturnFromageAndOignon () throws SQLException {
    List<Ingredient> newIngredients = List.of(
        new Ingredient("Fromage", CategoryEnum.DAIRY, 1200.0),
        new Ingredient("Oignon", CategoryEnum.VEGETABLE, 500.0)
    );

    List<Ingredient> createdIngredients = dataRetriever.createIngredients(newIngredients);
    Assertions.assertEquals("Fromage", createdIngredients.get(0).getName());
    Assertions.assertEquals(CategoryEnum.DAIRY, createdIngredients.get(0).getCategory());
    Assertions.assertEquals(1200.0, createdIngredients.get(0).getPrice());

    Assertions.assertEquals("Oignon", createdIngredients.get(1).getName());
    Assertions.assertEquals(CategoryEnum.VEGETABLE, createdIngredients.get(1).getCategory());
    Assertions.assertEquals(500.0, createdIngredients.get(1).getPrice());
  }

  @Test(expected = RuntimeException.class)
  public void createIngredients_shouldThrowException_whenDuplicateIngredient() throws SQLException{
    List<Ingredient> newIngredients = List.of(
        new Ingredient("Carotte", CategoryEnum.VEGETABLE, 2000.0),
        new Ingredient("Laitue", CategoryEnum.VEGETABLE, 2000.0)
    );

    dataRetriever.createIngredients(newIngredients);
  }
}
