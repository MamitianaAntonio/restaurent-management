import org.antonio.Entity.model.CategoryEnum;
import org.antonio.Entity.service.DataRetriever;
import org.antonio.Entity.model.Dish;
import org.antonio.Entity.model.Ingredient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.List;

public class IngredientTest {
  private DataRetriever dataRetriever = new DataRetriever();

  @Test
  public void findIngredient_page2size2_shouldReturnPouletAndChocolat() throws SQLException {
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
  public void createIngredients_shouldReturnFromageAndOignon() throws SQLException {
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
  public void createIngredients_shouldThrowException_whenDuplicateIngredient() throws SQLException {
    List<Ingredient> newIngredients = List.of(
        new Ingredient("Carotte", CategoryEnum.VEGETABLE, 2000.0),
        new Ingredient("Laitue", CategoryEnum.VEGETABLE, 2000.0)
    );

    dataRetriever.createIngredients(newIngredients);
  }

  @Test
  public void testFindDishesByIngredientName() throws SQLException {
    List<Dish> result = dataRetriever.findDishesByIngredientName("eur");
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("Gâteau au chocolat", result.get(0).getName());
  }

  // Test f) Filtre par catégorie VEGETABLE
  @Test
  public void findIngredientsByCriteria_shouldReturnLaitueAndTomate() throws Exception {
    // GIVEN
    String ingredientName = null;
    CategoryEnum category = CategoryEnum.VEGETABLE;
    String dishName = null;
    int page = 1;
    int size = 10;

    // WHEN
    List<Ingredient> result = dataRetriever.findIngredientsByCriteria(ingredientName, category, dishName, page, size);

    // THEN
    Assertions.assertNotNull(result);
    Assertions.assertEquals(2, result.size());
    Assertions.assertTrue(result.stream().anyMatch(i -> i.getName().equals("Laitue")));
    Assertions.assertTrue(result.stream().anyMatch(i -> i.getName().equals("Tomate")));
  }

  // Test g) Pas de résultat attendu
  @Test
  public void findIngredientsByCriteria_shouldReturnEmptyList() throws Exception {
    String ingredientName = "cho";
    CategoryEnum category = null;
    String dishName = "Sal";
    int page = 1;
    int size = 10;

    List<Ingredient> result = dataRetriever.findIngredientsByCriteria(ingredientName, category, dishName, page, size);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void findIngredientsByCriteria_shouldReturnChocolat() throws Exception {
    String ingredientName = "cho";
    CategoryEnum category = null;
    String dishName = "gâteau";
    int page = 1;
    int size = 10;

    List<Ingredient> result = dataRetriever.findIngredientsByCriteria(ingredientName, category, dishName, page, size);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.size());
  }
}