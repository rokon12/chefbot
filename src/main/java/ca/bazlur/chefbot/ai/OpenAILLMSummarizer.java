package ca.bazlur.chefbot.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.V;

import java.util.List;

public class OpenAILLMSummarizer implements Summarizer {

    private final int desiredTokenLimit;
    private final SummarizerAssistant assistant;

    public OpenAILLMSummarizer(OpenAiChatModel openAiChatModel, int desiredTokenLimit) {
        this.desiredTokenLimit = desiredTokenLimit;
        assistant = AiServices.builder(SummarizerAssistant.class)
                .chatLanguageModel(openAiChatModel)
                .build();
    }

    @Override
    public String summarize(List<ChatMessage> messages) {
        StringBuilder promptBuilder = new StringBuilder("Summarize the following conversation: \n");
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage) {
                promptBuilder.append("User: ").append(((UserMessage) msg).contents()).append("\n");
            } else if (msg instanceof AiMessage) {
                promptBuilder.append("Assistant: ").append(((AiMessage) msg).text()).append("\n");
            } else if (msg instanceof SystemMessage) {
                promptBuilder.append("System: ").append(((SystemMessage) msg).text()).append("\n");
            }
        }

        String summary = assistant.summarize(promptBuilder.toString(), desiredTokenLimit);
        return summary.trim();
    }

    interface SummarizerAssistant {
        @dev.langchain4j.service.UserMessage("""
                Summarize the following conversation: \
                
                {message}
                
                Shortly summarize the main points from the following {{message}}  in under {desiredTokenLimit} tokens.""")
        String summarize(@V("message") String messages, @V("desiredTokenLimit") int desiredTokenLimit);
    }
}