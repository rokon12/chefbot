package ca.bazlur.chefbot;

import ca.bazlur.chefbot.core.RecipeBot;
import ca.bazlur.chefbot.domain.model.Recipe;
import ca.bazlur.chefbot.domain.model.Conversation;
import ca.bazlur.chefbot.domain.model.BotResponse;
import ca.bazlur.chefbot.domain.service.RecipeFormatter;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // Get OpenAI API key from environment variable
        String openAiApiKey = System.getenv("OPENAI_API_KEY");
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set");
            System.exit(1);
        }

        // Initialize the recipe bot
        RecipeBot recipeBot = RecipeBot.create(openAiApiKey);
        Scanner scanner = new Scanner(System.in);

        // Display initial greeting
        System.out.println(recipeBot.getInitialGreeting());

        // Main conversation loop
        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine().trim();

            // Check for exit command
            if (userInput.equalsIgnoreCase("exit") ||
                userInput.equalsIgnoreCase("quit") ||
                userInput.equalsIgnoreCase("bye")) {
                System.out.println("\nBot: Goodbye! Enjoy your cooking adventures!");
                break;
            }

            // Process user input and get bot's response
            try {
                BotResponse botResponse = recipeBot.processUserInput(userInput);
                if (botResponse instanceof Recipe foodRecipe) {
                    RecipeFormatter.printRecipe(foodRecipe);
                } else if (botResponse instanceof Conversation conversation) {
                    System.out.println("\nBot: " + conversation.getMessage());
                }
            } catch (Exception e) {
                System.err.println("\nAn error occurred: " + e.getMessage());
                System.out.println("\nBot: I apologize, but I encountered an error. " +
                                 "Please try again or rephrase your request.");
            }
        }

        scanner.close();
    }
}
