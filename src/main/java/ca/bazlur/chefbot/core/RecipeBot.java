package ca.bazlur.chefbot.core;

import ca.bazlur.chefbot.ai.OpenAILLMSummarizer;
import ca.bazlur.chefbot.ai.SummarizingTokenWindowChatMemory;
import ca.bazlur.chefbot.api.RecipeBotAssistant;
import ca.bazlur.chefbot.domain.model.BotResponse;
import ca.bazlur.chefbot.domain.model.Conversation;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecipeBot {
    public static final String MODEL_NAME = "gpt-4o";
    private final RecipeBotAssistant recipeBotAssistant;
    private final RecipeExtractor recipeExtractor;

    public RecipeBot(String openAiApiKey) {

        OpenAiChatModel openAiModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(MODEL_NAME)
                .temperature(0.7)
                //.logRequests(true)
                //.logResponses(true)
                .build();

        this.recipeBotAssistant = AiServices.builder(RecipeBotAssistant.class)
                .chatLanguageModel(openAiModel)
                .chatMemory(SummarizingTokenWindowChatMemory.builder()
                        .id("recipe-bot")
                        .maxTokens(1000, new OpenAiTokenizer(MODEL_NAME))
                        .chatMemoryStore(new InMemoryChatMemoryStore())
                        .summarizer(new OpenAILLMSummarizer(openAiModel, 300))
                        .build())
                .build();

        this.recipeExtractor = AiServices.builder(RecipeExtractor.class)
                .chatLanguageModel(openAiModel)
                .build();
    }

    public BotResponse processUserInput(String userInput) {
        try {
            String response = recipeBotAssistant.getRecipe(userInput);
            if (isRecipe(response)) {
                return recipeExtractor.extractRecipe(response);
            } else {
                return new Conversation(response);
            }

        } catch (Exception e) {
            log.error("Error processing user input", e);
            return new Conversation("I apologize, but I encountered an error. Could you please rephrase your request?");
        }
    }

    public String getInitialGreeting() {
        return """
                Hello! I'm your personal recipe recommendation assistant.\s
                To help you better, could you tell me about any dietary restrictions\s
                (e.g., vegetarian, vegan, gluten-free), preferred cuisines, or food allergies you have?""";
    }

    public static boolean isRecipe(String input) {
        return input.contains("[END OF RECIPE]");
    }

    public static boolean isJsonValid(String input) {
        try {
            JsonElement element = JsonParser.parseString(input);
            return element.isJsonObject() || element.isJsonArray();
        } catch (Exception e) {
            return false;
        }
    }
}