package ca.bazlur.chefbot.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAILLMSummarizerTest {

    private OpenAiChatModel openAiChatModel;
    private OpenAILLMSummarizer summarizer;
    private ChatResponse mockResponse;

    @BeforeEach
    void setUp() {
        openAiChatModel = Mockito.mock(OpenAiChatModel.class);
        summarizer = new OpenAILLMSummarizer(openAiChatModel, 100);

        mockResponse = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(mockResponse.metadata()).thenReturn(metadata);
        when(metadata.tokenUsage()).thenReturn(new TokenUsage(100, 10));
        when(openAiChatModel.chat(any(ChatRequest.class))).thenReturn(mockResponse);
    }

    @Test
    void shouldSummarizeMessages() {
        List<ChatMessage> messages = List.of(
                UserMessage.from("Hello"),
                AiMessage.from("Hi there! How can I help you today?"),
                SystemMessage.from("System message")
        );

        when(mockResponse.aiMessage()).thenReturn(AiMessage.from("Summary: User greeted and assistant responded."));

        String summary = summarizer.summarize(messages);

        assertThat(summary).isEqualTo("Summary: User greeted and assistant responded.");
    }
}