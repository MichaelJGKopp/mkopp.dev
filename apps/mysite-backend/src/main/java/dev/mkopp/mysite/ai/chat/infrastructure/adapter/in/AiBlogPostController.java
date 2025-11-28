package dev.mkopp.mysite.ai.chat.infrastructure.adapter.in;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.mkopp.mysite.ai.chat.infrastructure.adapter.SystemPrompts;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/ai/blog-post")
@RequiredArgsConstructor
public class AiBlogPostController {
    // private final AiBlogPostService aiBlogPostService;

    private final Map<String, ChatClient> chatClients;
 
    @GetMapping("/new")
    public AiBlogPostResponse newPost(
            @RequestParam String topic,
            @RequestParam(defaultValue = "geminiChatClient") String clientBean) {
        ChatClient chatClient = getChatClient(clientBean);
        String system = SystemPrompts.BLOG_POST_AUTHOR_INSTRUCTIONS;

        return chatClient.prompt()
                .system(system)
                .user(u -> {
                    u.text("Write me a blog post about {topic}");
                    u.param("topic", topic);
                })
                .call()
                .entity(AiBlogPostResponse.class);
    }

    private ChatClient getChatClient(String clientBean) {
        ChatClient chatClient = chatClients.get(clientBean);
        if (chatClient == null) {
            throw new IllegalArgumentException("No ChatClient bean found with name: " + clientBean);
        }
        return chatClient;
    }
}