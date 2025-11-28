package dev.mkopp.mysite.ai.chat.infrastructure.adapter.in;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.mkopp.mysite.ai.chat.infrastructure.adapter.SystemPrompts;
import dev.mkopp.mysite.ai.chat.infrastructure.adapter.out.DateTimeTools;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/ai/chat")
@RequiredArgsConstructor
public class AiChatController {
        // private final AiChatService aiChatService;

        private final Map<String, ChatClient> chatClients;
        private final ChatMemory chatMemory;
        private final VectorStore vectorStore;
        private final DateTimeTools dateTimeTools;

        @GetMapping("/info")
        public List<String> info(@RequestParam(defaultValue = "geminiChatClient") String clientBean) {
                ChatClient chatClient = getChatClient(clientBean);
                return List.of(
                                "GeminiBaseUrl: " + System.getenv("GEMINI_BASE_URL"),
                                "GeminiModel: " + System.getenv("GEMINI_MODEL"),
                                chatClient.toString());
        }

        @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> chat(
                        @RequestParam(defaultValue = "Hello, how can AI assist me today?") String message,
                        @RequestParam(defaultValue = "global") String conversationId,
                        @RequestParam(defaultValue = "geminiChatClient") String clientBean) {
                ChatClient chatClient = getChatClient(clientBean);
                String systemInstructions = SystemPrompts.ASSISTANT_INSTRUCTIONS;

                String chatResponse = chatClient.prompt()
                                .system(systemInstructions)
                                .advisors(
                                        MessageChatMemoryAdvisor.builder(chatMemory)
                                                .conversationId(conversationId).build()
                                        //         ,
                                        // QuestionAnswerAdvisor.builder(vectorStore).build()
                                )
                                .user(message)
                                // .tools(dateTimeTools)     // adding global Tool Bean, can be also created manually
                                .call()
                                .content();
                return ResponseEntity.ok(chatResponse);
        }

        @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<AiChatMessageResponseDto>> history(
                        @RequestParam(defaultValue = "global") String conversationId) {
                List<AiChatMessageResponseDto> history = chatMemory.get(conversationId).stream()
                                .map(msg -> new AiChatMessageResponseDto(
                                                msg.getMessageType().toString(),
                                                msg.getText()))
                                .toList();
                return ResponseEntity.ok(history);
        }

        @GetMapping("/history-full")
        public ResponseEntity<List<Message>> historyFull(@RequestParam(defaultValue = "global") String conversationId) {
                List<Message> historyFull = chatMemory.get(conversationId).stream()
                                .map(msg -> msg)
                                .toList();
                return ResponseEntity.ok(historyFull);
        }

        @GetMapping("/chat-response")
        public ChatResponse chatResponse(
                        @RequestParam(defaultValue = "Hello, how can AI assist me today?") String message,
                        @RequestParam(defaultValue = "geminiChatClient") String clientBean) {
                ChatClient chatClient = getChatClient(clientBean);
                return chatClient.prompt()
                                .user(message)
                                .call()
                                .chatResponse();
        }

        // needs webflux dependency
        @GetMapping("/stream")
        public Flux<String> stream(
                        @RequestParam(defaultValue = "Hello, how can AI assist me today?") String message,
                        @RequestParam(defaultValue = "geminiChatClient") String clientBean) {
                ChatClient chatClient = getChatClient(clientBean);
                return chatClient.prompt()
                                .user(message)
                                .stream()
                                .content();
        }

        private ChatClient getChatClient(String clientBean) {
                ChatClient chatClient = chatClients.get(clientBean);
                if (chatClient == null) {
                        throw new IllegalArgumentException("No ChatClient bean found with name: " + clientBean);
                }
                return chatClient;
        }
}