package ca.bazlur.chefbot.ai;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface Summarizer {
    String summarize(List<ChatMessage> messages);
}