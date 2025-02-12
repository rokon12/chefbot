# ChefBot - AI-Powered Recipe Assistant

ChefBot is an intelligent recipe recommendation chatbot powered by OpenAI's GPT-4 model. It provides personalized recipe suggestions based on your dietary preferences, restrictions, and culinary interests.

## Features

- 🤖 AI-powered recipe recommendations
- 🍽️ Personalized suggestions based on dietary restrictions
- 🌟 Interactive conversation with memory
- 🔄 Automatic conversation summarization to maintain context
- ⚡ Real-time recipe extraction and formatting

## Prerequisites

- Java 21 or higher
- Gradle
- OpenAI API Key

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/chefbot.git
   cd chefbot
   ```

2. Set up your OpenAI API key:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```

## Usage

1. Run the application:
   ```bash
   java -cp build/libs/chefbot-1.0-SNAPSHOT.jar ca.bazlur.chefbot.Main
   ```

   Alternatively, you can run it directly through your IDE by executing the `Main` class.

2. The bot will greet you and ask about your dietary preferences.

3. You can then:
   - Ask for specific recipes
   - Request recommendations based on ingredients
   - Inquire about dietary modifications
   - Ask for cooking tips and techniques

## Project Structure

```
src/main/java/ca/bazlur/chefbot/
├── Main.java                    # Application entry point
├── ai/
│   ├── OpenAILLMSummarizer.java # Conversation summarization
│   └── Summarizer.java          # Summarization interface
├── api/
│   └── RecipeBotAssistant.java  # AI assistant interface
├── core/
│   └── RecipeBot.java          # Core bot implementation
└── domain/
    ├── model/                   # Data models
    └── service/                 # Business logic services
```

## Dependencies

- LangChain4j (0.36.2) - AI/LLM integration framework
- OpenAI Integration for LangChain4j
- Lombok - Reduces boilerplate code
- SLF4J & Logback - Logging
- JUnit & Mockito - Testing

## How It Works

ChefBot uses OpenAI's GPT-4 model through LangChain4j to provide intelligent recipe recommendations. It maintains conversation context using a token window memory system and automatically summarizes older conversations to stay within token limits while preserving important context.

The bot can:
1. Understand and remember your dietary preferences
2. Provide detailed recipes with ingredients and instructions
3. Answer questions about cooking techniques
4. Modify recipes based on dietary restrictions
5. Offer alternative ingredients and substitutions

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
