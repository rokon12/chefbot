package ca.bazlur.chefbot.domain.model;

public sealed interface BotResponse permits Conversation, Recipe {
}
