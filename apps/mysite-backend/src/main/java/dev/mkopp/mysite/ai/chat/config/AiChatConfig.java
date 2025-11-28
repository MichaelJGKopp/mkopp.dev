package dev.mkopp.mysite.ai.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatClientModel) {
        return ChatClient.create(chatClientModel);
    }

    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(OpenAiChatModel  chatClientModel) {
        return ChatClient.builder(chatClientModel)
        // .defaultSystem("...You are a helpful assistant.")
        .build();
    }


}
