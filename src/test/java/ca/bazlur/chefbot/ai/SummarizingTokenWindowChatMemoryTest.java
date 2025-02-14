package ca.bazlur.chefbot.ai;

import dev.langchain4j.data.message.*;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SummarizingTokenWindowChatMemoryTest {

    private static final int MAX_TOKENS = 100;

    private Summarizer summarizer;
    private Tokenizer tokenizer;
    private InMemoryChatMemoryStore store;

    @BeforeEach
    void setUp() {
        summarizer = mock(Summarizer.class);
        tokenizer = mock(Tokenizer.class);
        store = new InMemoryChatMemoryStore();

        // Default mock behavior: each message => 1 token.
        // Summaries => 1 token, etc. Adjust this as needed.
        when(tokenizer.estimateTokenCountInMessages(Mockito.anyList())).thenAnswer(invocation -> {
            List<ChatMessage> messages = invocation.getArgument(0);
            // Let's assume each message is 10 tokens for test simplicity,
            // or each message is 1 token to simplify even further.
            // We'll use 10 tokens here to show some variety:
            return messages.size() * 10;
        });

        // Default summarizer returns a fixed summary
        when(summarizer.summarize(anyList())).thenReturn("This is a mock summary.");
    }

    private SummarizingTokenWindowChatMemory createMemory() {
        return SummarizingTokenWindowChatMemory.builder()
                .id("test-session")
                .maxTokens(MAX_TOKENS, tokenizer)
                .chatMemoryStore(store)
                .summarizer(summarizer)
                .build();
    }

    @Nested
    class SystemMessageTests {

        @Test
        void shouldAddSystemMessageWhenNoneExists() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            SystemMessage systemMessage = SystemMessage.from("Initial System Message");
            memory.add(systemMessage);

            List<ChatMessage> messages = memory.messages();

            assertThat(messages).hasSize(1);
            assertThat(messages.getFirst()).isEqualTo(systemMessage);
        }

        @Test
        void shouldReplaceExistingSystemMessageWithNewOne() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            SystemMessage oldSystemMessage = SystemMessage.from("Old System Message");
            memory.add(oldSystemMessage);

            SystemMessage newSystemMessage = SystemMessage.from("New System Message");
            memory.add(newSystemMessage);

            List<ChatMessage> messages = memory.messages();
            assertThat(messages).hasSize(1);
            assertThat(messages.getFirst()).isEqualTo(newSystemMessage);
        }

        @Test
        void shouldIgnoreAddingSameSystemMessage() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            SystemMessage systemMessage = SystemMessage.from("System Message");
            memory.add(systemMessage);

            // Add the same message
            memory.add(systemMessage);

            List<ChatMessage> messages = memory.messages();
            // Should still have only 1 system message
            assertThat(messages).hasSize(1);
            assertThat(messages.getFirst()).isEqualTo(systemMessage);
        }
    }

    @Nested
    class SummarizationTests {

        @Test
        void shouldNotSummarizeIfBelowMaxTokens() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            // Each message is ~10 tokens (based on our mock).
            // Adding 9 messages => 90 tokens total => still under 100
            for (int i = 0; i < 9; i++) {
                memory.add(UserMessage.from("User message " + i));
            }

            // Summarization should not occur because 9*10 = 90 <= 100
            List<ChatMessage> messages = memory.messages();
            assertThat(messages).hasSize(9);
            // No system summary message should be present
            assertThat(messages.stream().filter(m -> m instanceof SystemMessage).count()).isZero();
        }

        @Test
        void shouldSummarizeOlderMessagesIfOverMaxTokens() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            // Each message => 10 tokens, so 11 messages = 110 tokens > 100
            for (int i = 0; i < 11; i++) {
                memory.add(UserMessage.from("User message " + i));
            }
            // Summarization should happen now

            List<ChatMessage> messages = memory.messages();
            // We expect:
            //   1) a SystemMessage containing the summary
            //   2) The last (11th) message remains as user message for context
            // Because we said "summarize everything except the last message"
            // The final token count should be <= 100.

            // Let's see how the logic works:
            // - The code separates out older messages except maybe the last one.
            // - Summarize them -> single system message with summary
            // - Then re-check capacity.
            //   We'll have 1 summarized system message (10 tokens) + 1 user message (10 tokens) = 20 tokens total.
            //   That is under 100, so we are good.

            // So we should end up with 2 messages total:
            //   a) SystemMessage("Previous conversation summary: ...")
            //   b) Last user message: "User message 10"
            assertThat(messages).hasSize(2);

            ChatMessage first = messages.getFirst();
            assertThat(first).isInstanceOf(SystemMessage.class);
            SystemMessage systemMsg = (SystemMessage) first;
            assertThat(systemMsg.text()).contains("Previous conversation summary: This is a mock summary.");

            ChatMessage second = messages.get(1);
            assertThat(second).isInstanceOf(UserMessage.class);
            assertThat(getMessageContent(second)).isEqualTo("User message 10");
        }

        @Test
        void shouldRemoveOldestMessagesIfTooFewToSummarize() {
            SummarizingTokenWindowChatMemory memory = createMemory();

            // We'll add just 2 messages, then each is 10 tokens => 20 tokens total
            // Now add a new message that pushes us over 100 tokens artificially:
            // Let's tweak the tokenizer mock to force an overflow scenario even with minimal messages.
            when(tokenizer.estimateTokenCountInMessages(Mockito.anyList())).thenReturn(120);

            memory.add(UserMessage.from("First message"));
            memory.add(AiMessage.from("Second message"));

            // Now we're at 120 (mocked). Summarizing logic tries to summarize older messages
            // but if we have only 2 messages (startIndex=0, endIndex=1 => difference = 1),
            // the logic says "if (endIndex - startIndex <= 1) remove oldest messages until fit."

            List<ChatMessage> messagesBefore = memory.messages();
            // Actually calling memory.messages() again triggers ensureSummarizedCapacity internally
            // That should cause the oldest messages to be removed until we are under the token limit.

            // Because the mock says 120 tokens with 2 messages, removing the oldest one should leave 1 message => 10 tokens, which is still >100
            // So we remove both until we are at 0 messages => 0 tokens, which is < 100

            List<ChatMessage> messagesAfter = memory.messages();
            assertThat(messagesAfter).isEmpty();
        }

        @Test
        void shouldAlsoPreserveSystemMessageDuringRemovalIfAny() {
            SummarizingTokenWindowChatMemory memory = createMemory();
            memory.add(SystemMessage.from("System message"));
            memory.add(UserMessage.from("User message"));

            // Force the capacity check to fail
            when(tokenizer.estimateTokenCountInMessages(Mockito.anyList())).thenReturn(120);

            // Trigger summarization logic
            List<ChatMessage> messages = memory.messages();

            // The system message should remain
            assertThat(messages).hasSize(1);
            assertThat(messages.getFirst()).isInstanceOf(SystemMessage.class);
            assertThat(getContentOfFirstMessage(messages)).isEqualTo("System message");
        }
    }

    private String getContentOfFirstMessage(List<ChatMessage> messages) {
        ChatMessage first = messages.getFirst();
        return getMessageContent(first);
    }

    private String getMessageContent(ChatMessage first) {
        return switch (first) {
            case SystemMessage systemMessage -> systemMessage.text();
            case UserMessage userMessage -> userMessage.singleText();
            case AiMessage aiMessage -> aiMessage.text();
            case ToolExecutionResultMessage toolExecutionResultMessage -> toolExecutionResultMessage.text();
            case CustomMessage customMessage -> customMessage.text();
            default -> throw new IllegalArgumentException("Unexpected message type: " + first.type());
        };
    }

    @Test
    void shouldClearMessages() {
        SummarizingTokenWindowChatMemory memory = createMemory();
        memory.add(SystemMessage.from("System message"));
        memory.add(UserMessage.from("Hello"));

        assertThat(memory.messages()).hasSize(2);

        memory.clear();
        assertThat(memory.messages()).isEmpty();
    }
}
