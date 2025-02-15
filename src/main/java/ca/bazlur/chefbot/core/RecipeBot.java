package ca.bazlur.chefbot.core;

import ca.bazlur.chefbot.ai.OpenAILLMSummarizer;
import ca.bazlur.chefbot.ai.SummarizingTokenWindowChatMemory;
import ca.bazlur.chefbot.config.EnvironmentConfig;
import ca.bazlur.chefbot.api.RecipeBotAssistant;
import ca.bazlur.chefbot.domain.model.BotResponse;
import ca.bazlur.chefbot.domain.model.Conversation;
import ca.bazlur.chefbot.domain.model.Recipe;
import com.google.gson.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
public class RecipeBot {
    private static final String MODEL_NAME = EnvironmentConfig.getEnv("OPENAI_MODEL_NAME", "gpt-4o");
    private static final int MAX_TOKENS = EnvironmentConfig.getEnvAsInt("OPENAI_MAX_TOKENS", 4000);
    private static final double TEMPERATURE = EnvironmentConfig.getEnvAsDouble("OPENAI_TEMPERATURE", 0.7);

    private final RecipeBotAssistant recipeBotAssistant;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BotResponse.class, new BotResponseDeserializer())
            .create();

    public RecipeBot(RecipeBotAssistant recipeBotAssistant) {
        this.recipeBotAssistant = recipeBotAssistant;
    }

    public static RecipeBot create(String openAiApiKey) {
        OpenAiChatModel openAiModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(MODEL_NAME)
                .temperature(TEMPERATURE)
                .build();

        RecipeBotAssistant assistant = AiServices.builder(RecipeBotAssistant.class)
                .chatLanguageModel(openAiModel)
                .chatMemory(SummarizingTokenWindowChatMemory.builder()
                        .id("recipe-bot")
                        .maxTokens(MAX_TOKENS, new OpenAiTokenizer(MODEL_NAME))
                        .chatMemoryStore(new InMemoryChatMemoryStore())
                        .summarizer(new OpenAILLMSummarizer(openAiModel, 300))
                        .build())
                .build();

        return new RecipeBot(assistant);
    }

    public BotResponse processUserInput(String userInput) {
        try {
            String response = recipeBotAssistant.getRecipe(userInput);
            return gson.fromJson(response, BotResponse.class);
        } catch (Exception e) {
            log.error("Error processing user input", e);
            return new Conversation("I apologize, but I encountered an error. Could you please rephrase your request?");
        }
    }

    static class BotResponseDeserializer implements JsonDeserializer<BotResponse> {

        @Override
        public BotResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            return switch (type) {
                case "recipe" -> context.deserialize(jsonObject, Recipe.class);
                case "conversation" -> context.deserialize(jsonObject, Conversation.class);
                default -> throw new JsonParseException("Unknown BotResponse type: " + type);
            };
        }
    }

    public String getInitialGreeting() {
        return """
                Hello! I'm your personal recipe recommendation assistant.\s
                To help you better, could you tell me about any dietary restrictions\s
                (e.g., vegetarian, vegan, gluten-free), preferred cuisines, or food allergies you have?""";
    }
}
