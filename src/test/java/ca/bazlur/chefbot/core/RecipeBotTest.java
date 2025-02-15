package ca.bazlur.chefbot.core;

import ca.bazlur.chefbot.api.RecipeBotAssistant;
import ca.bazlur.chefbot.domain.model.BotResponse;
import ca.bazlur.chefbot.domain.model.Conversation;
import ca.bazlur.chefbot.domain.model.Recipe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeBotTest {

    @Mock
    private RecipeBotAssistant recipeBotAssistant;

    private RecipeBot recipeBot;

    @BeforeEach
    void setUp() {
        recipeBot = new RecipeBot(recipeBotAssistant);
    }

    @Test
    void getInitialGreeting_shouldReturnWelcomeMessage() {
        String greeting = recipeBot.getInitialGreeting();

        assertThat(greeting)
            .contains("Hello")
            .contains("recipe recommendation assistant")
            .contains("dietary restrictions");
    }

    @Test
    void processUserInput_whenRecipeResponse_shouldReturnRecipe() {
        String recipeJson = """
            {
                "type": "recipe",
                "name": "Spaghetti Carbonara",
                "ingredients": ["pasta", "eggs", "cheese"],
                "instructions": ["Cook pasta", "Mix eggs and cheese"]
            }
            """;

        when(recipeBotAssistant.getRecipe(anyString())).thenReturn(recipeJson);

        BotResponse response = recipeBot.processUserInput("How to make carbonara?");

        assertThat(response)
            .isInstanceOf(Recipe.class);
        Recipe recipe = (Recipe) response;
        assertThat(recipe.getName()).isEqualTo("Spaghetti Carbonara");
        assertThat(recipe.getIngredients()).contains("pasta", "eggs", "cheese");
    }

    @Test
    void processUserInput_whenConversationResponse_ShouldReturnConversation() {
        String conversationJson = """
            {
                "type": "conversation",
                "message": "I can help you find a recipe. What would you like to cook?"
            }
            """;

        when(recipeBotAssistant.getRecipe(anyString())).thenReturn(conversationJson);

        BotResponse response = recipeBot.processUserInput("Hi");

        assertThat(response)
            .isInstanceOf(Conversation.class);
        Conversation conversation = (Conversation) response;
        assertThat(conversation.getMessage())
            .contains("I can help you find a recipe");
    }

    @Test
    void processUserInput_whenException_shouldReturnErrorMessage() {
        when(recipeBotAssistant.getRecipe(anyString())).thenThrow(new RuntimeException("API Error"));

        BotResponse response = recipeBot.processUserInput("How to make pizza?");

        assertThat(response)
            .isInstanceOf(Conversation.class);
        Conversation conversation = (Conversation) response;
        assertThat(conversation.getMessage())
            .contains("I apologize")
            .contains("error");
    }
}
