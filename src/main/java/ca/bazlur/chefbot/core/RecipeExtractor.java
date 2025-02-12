package ca.bazlur.chefbot.core;

import ca.bazlur.chefbot.domain.model.Recipe;
import dev.langchain4j.service.UserMessage;

public interface RecipeExtractor {
    @UserMessage("""
            Extract the info for the Recipe described below.
            Return strictly only JSON, without any markdown markup surrounding it.
            Here is the document describing the Recipe:
            ---
            {{it}}""")
    Recipe extractRecipe(String text);
}