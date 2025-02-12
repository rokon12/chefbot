package ca.bazlur.chefbot.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SummarizingTokenWindowChatMemory implements ChatMemory {
    private static final Logger log = LoggerFactory.getLogger(SummarizingTokenWindowChatMemory.class);

    private final Object id;
    private final int maxTokens;
    private final Tokenizer tokenizer;
    private final ChatMemoryStore store;
    private final Summarizer summarizer;

    private SummarizingTokenWindowChatMemory(Builder builder) {
        this.id = ValidationUtils.ensureNotNull(builder.id, "id");
        this.maxTokens = ValidationUtils.ensureGreaterThanZero(builder.maxTokens, "maxTokens");
        this.tokenizer = ValidationUtils.ensureNotNull(builder.tokenizer, "tokenizer");
        this.store = ValidationUtils.ensureNotNull(builder.store, "store");
        this.summarizer = ValidationUtils.ensureNotNull(builder.summarizer, "summarizer");
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    public void add(ChatMessage message) {
        // Pull existing messages from store
        List<ChatMessage> messages = new ArrayList<>(store.getMessages(id));

        // If it's a system message, handle "replace existing system message" logic
        if (message instanceof SystemMessage) {
            Optional<SystemMessage> maybeSystem = findSystemMessage(messages);
            if (maybeSystem.isPresent()) {
                if (maybeSystem.get().equals(message)) {
                    // Same system message, do nothing
                    return;
                } else {
                    // Remove old system message so we can replace with new one
                    messages.remove(maybeSystem.get());
                }
            }
        }

        // Add the new message
        messages.add(message);

        // Enforce capacity by summarizing older messages if needed
        ensureSummarizedCapacity(messages);

        // Update store
        store.updateMessages(id, messages);
    }

    @Override
    public List<ChatMessage> messages() {
        // Return a copy of messages from store
        List<ChatMessage> messages = new ArrayList<>(store.getMessages(id));

        // (Optional) ensure capacity here again, if you want to guarantee it every time
        ensureSummarizedCapacity(messages);
        return messages;
    }

    @Override
    public void clear() {
        store.deleteMessages(id);
    }

    private void ensureSummarizedCapacity(List<ChatMessage> messages) {
        int currentTokenCount = tokenizer.estimateTokenCountInMessages(messages);
        if (currentTokenCount <= maxTokens) {
            return; // We are within capacity
        }

        // If we exceed tokens, let's summarize the older messages (except system msg & possibly the newest).
        // 1) Separate out the system message if present at index 0.
        // 2) Summarize everything from startIndex...up to near the end,
        //    leaving maybe the last user or assistant message "unsummarized" for context.
        // 3) Insert the summary as a single message, then re-check capacity.

        // First, handle any system message
        Optional<SystemMessage> maybeSystem = findSystemMessage(messages);
        maybeSystem.ifPresent(messages::remove);

        // Now we can work with the non-system messages
        int startIndex = 0;
        int endIndex = messages.size() - 1; // Leave the last message for context

        // Don't try to summarize if we have 2 or fewer messages
        if (endIndex - startIndex <= 1) {
            // If we can't summarize, fall back to just removing oldest messages
            removeOldestUntilFit(messages);
            // Re-add system message if we had one
            maybeSystem.ifPresent(messages::addFirst);
            return;
        }

        // Get the messages to summarize (everything except maybe system & last)
        List<ChatMessage> toSummarize = new ArrayList<>(messages.subList(startIndex, endIndex));

        // Generate the summary
        String summary = summarizer.summarize(toSummarize);

        // Replace the summarized messages with the summary
        messages.subList(startIndex, endIndex).clear();
        messages.add(startIndex, SystemMessage.from("Previous conversation summary: " + summary));

        // Re-add system message if we had one
        maybeSystem.ifPresent(messages::addFirst);

        // If we're still over capacity, remove oldest messages (after any system message)
        if (tokenizer.estimateTokenCountInMessages(messages) > maxTokens) {
            removeOldestUntilFit(messages);
        }
    }

    private void removeOldestUntilFit(List<ChatMessage> messages) {
        // Keep system message if present
        Optional<SystemMessage> maybeSystem = findSystemMessage(messages);
        maybeSystem.ifPresent(messages::remove);

        // Remove oldest messages until we're under token limit
        while (!messages.isEmpty() && tokenizer.estimateTokenCountInMessages(messages) > maxTokens) {
            messages.removeFirst();
        }

        // Re-add system message if we had one
        maybeSystem.ifPresent(messages::addFirst);
    }

    private Optional<SystemMessage> findSystemMessage(List<ChatMessage> messages) {
        if (!messages.isEmpty() && messages.getFirst() instanceof SystemMessage) {
            return Optional.of((SystemMessage) messages.getFirst());
        }
        return Optional.empty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;
        private Integer maxTokens;
        private Tokenizer tokenizer;
        private ChatMemoryStore store = new InMemoryChatMemoryStore();
        private Summarizer summarizer;

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder maxTokens(Integer maxTokens, Tokenizer tokenizer) {
            this.maxTokens = maxTokens;
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder chatMemoryStore(ChatMemoryStore store) {
            this.store = store;
            return this;
        }

        public Builder summarizer(Summarizer summarizer) {
            this.summarizer = summarizer;
            return this;
        }

        public SummarizingTokenWindowChatMemory build() {
            return new SummarizingTokenWindowChatMemory(this);
        }
    }
}