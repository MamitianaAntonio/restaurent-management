import org.antonio.Entity.DataRetriever;
import org.antonio.Entity.Dish;
import org.antonio.Entity.DishTypeEnum;
import org.antonio.Entity.Ingredient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DishTest {
  private final DataRetriever dataRetriever = new DataRetriever();

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

  @Test
  public void saveDish_shouldCreateSoupeDeLegumesWithOignon() throws SQLException {
    Ingredient oignon = new Ingredient();
    oignon.setId(6);
    oignon.setName("Oignon");

    Dish dish = new Dish();
    dish.setName("Soupe de légumes");
    dish.setDishType(DishTypeEnum.START);
    dish.setIngredients(List.of(oignon));

    Dish savedDish = dataRetriever.saveDish(dish);

    assertNotNull(savedDish.getId());
    assertEquals("Soupe de légumes", savedDish.getName());

    List<String> ingredientNames = savedDish.getIngredients()
        .stream()
        .map(Ingredient::getName)
        .toList();

    Assertions.assertEquals(1, ingredientNames.size());
    Assertions.assertTrue(ingredientNames.contains("Oignon"));
  }

  @Test
  public void saveDish_shouldRemoveIngredientsAndKeepOnlyFromage() throws SQLException {
    Ingredient fromage = new Ingredient();
    fromage.setName("Fromage");

    Dish dish = new Dish();
    dish.setId(1);
    dish.setName("Salade de fromage");
    dish.setDishType(DishTypeEnum.START);
    dish.setIngredients(List.of(fromage));

    Dish updatedDish = dataRetriever.saveDish(dish);

    List<String> ingredientNames = updatedDish.getIngredients()
        .stream()
        .map(Ingredient::getName)
        .toList();

    Assertions.assertEquals(1, ingredientNames.size());
    Assertions.assertTrue(ingredientNames.contains("Fromage"));
  }
}
