package ca.bazlur.chefbot.domain.model;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("A conversation message")
public final class Conversation implements BotResponse {
    private final String type = "conversation";

    private String message;
}