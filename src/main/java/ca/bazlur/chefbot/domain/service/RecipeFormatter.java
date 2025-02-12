package ca.bazlur.chefbot.domain.service;

import ca.bazlur.chefbot.domain.model.Recipe;

import java.util.Map;

public class RecipeFormatter {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String UNDERLINE = "\u001B[4m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";

    public static void printRecipe(Recipe recipe) {
        if (recipe == null) {
            System.out.println(RED + BOLD + "No recipe found." + RESET);
            return;
        }

        System.out.println(CYAN + BOLD + "--------------------------------------------------" + RESET);
        System.out.println(CYAN + BOLD + "Recipe: " + RESET + BOLD + recipe.getName() + RESET);
        System.out.println(CYAN + BOLD + "--------------------------------------------------" + RESET);

        if (recipe.getDescription() != null) {
            System.out.println("\n" + UNDERLINE + "Description:" + RESET + " " + recipe.getDescription());
        }

        if (recipe.getServingSize() != null) {
            System.out.println("\n" + UNDERLINE + "Serving Size:" + RESET + " " + BLUE + recipe.getServingSize() + " servings" + RESET);
        }

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            System.out.println("\n" + UNDERLINE + "Ingredients:" + RESET);
            for (String ingredient : recipe.getIngredients()) {
                System.out.println(GREEN + "✔ " + ingredient + RESET);
            }
        }

        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
            System.out.println("\n" + UNDERLINE + "Instructions:" + RESET);
            for (int i = 0; i < recipe.getInstructions().size(); i++) {
                System.out.println(YELLOW + (i + 1) + ". " + recipe.getInstructions().get(i) + RESET);
            }
        }

        if (recipe.getCuisineType() != null) {
            System.out.println("\n" + UNDERLINE + "Cuisine:" + RESET + " " + BLUE + recipe.getCuisineType() + RESET);
        }

        if (recipe.getDietaryRestrictions() != null && !recipe.getDietaryRestrictions().isEmpty()) {
            System.out.print("\n" + UNDERLINE + "Dietary Restrictions:" + RESET + " ");
            for (String restriction : recipe.getDietaryRestrictions()) {
                System.out.print(GREEN + restriction + RESET + ", ");
            }
            System.out.println();
        }

        System.out.println("\n" + UNDERLINE + "Spicy:" + RESET + " " + (recipe.isSpicy() ? RED + "Yes 🌶" + RESET : BLUE + "No" + RESET));

        if (recipe.getCalories() != null && !recipe.getCalories().isEmpty()) {
            System.out.println("\n" + UNDERLINE + "Calories:" + RESET);
            for (Map.Entry<String, String> entry : recipe.getCalories().entrySet()) {
                System.out.println(GREEN + entry.getKey() + ": " + entry.getValue() + " kcal" + RESET);
            }
        }

        System.out.println(CYAN + BOLD + "--------------------------------------------------" + RESET);
    }
}