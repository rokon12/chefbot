package ca.bazlur.chefbot.domain.model;

import jdk.jfr.Description;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Description("A recipe with ingredients, instructions, and other details")
public final class Recipe implements BotResponse {
    private String id;
    private String name;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private Map<String, String> calories;
    private String cuisineType;
    private Set<String> dietaryRestrictions;
    private boolean isSpicy;
    private String servingSize;
}