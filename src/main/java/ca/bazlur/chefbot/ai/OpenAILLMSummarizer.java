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
        You are a helpful assistant summarizing past conversation turns for a chatbot.

        Your goal is to create a concise and informative summary of the provided conversation history, 
        focusing on key information relevant to continuing the conversation.  
        Pay close attention to user preferences, requests, and any decisions they have made.  
        Also note any specific topics or tasks discussed.  Be sure to retain information that might be needed 
        to fulfill a user request or provide a relevant response.

        The summary should be under {desiredTokenLimit} tokens and written in a clear, natural language style.  
        Avoid simply listing the turns.  Instead, synthesize the information into a coherent narrative.

        Conversation History:
        {message}
        """)
        String summarize(@V("message") String messages, @V("desiredTokenLimit") int desiredTokenLimit);
    }
}