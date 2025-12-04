---
title: Building an AI-Powered Chatbot with Spring AI and Google Gemini
slug: ai-chatbot-spring-ai-gemini
author: Michael Kopp
publishedAt: 2025-12-04T10:00:00Z
tags:
  - spring-ai
  - gemini
  - ai
  - chatbot
  - spring-boot
  - rag
description: How I integrated an AI-powered chatbot using Spring AI 1.1.0 with Google Gemini and OpenAI support, including conversation memory, system prompts, and RAG architecture.
type: technical
status: draft
---

---

## Introduction

In this post, I'll walk through how I integrated an AI-powered chatbot into my portfolio website using Spring AI 1.1.0. The implementation supports multiple LLM providers (Google Gemini, OpenAI, and local models), conversation memory, system prompts with guardrails, and includes a skeleton for Retrieval Augmented Generation (RAG).

## Why Spring AI?

When I decided to add AI capabilities to my portfolio, I evaluated several options:

1. **LangChain4j**: Mature Java library with extensive features
2. **Direct API Integration**: Using OpenAI or Gemini SDKs directly
3. **Spring AI**: Spring's first-party AI integration framework

I chose Spring AI for several reasons:

- **Spring Ecosystem Integration**: Seamless integration with Spring Boot, dependency injection, and configuration
- **Multiple Provider Support**: Easy switching between Gemini, OpenAI, and local LLMs without code changes
- **Active Development**: Backed by VMware/Spring team with rapid feature additions
- **Unified API**: Common abstraction layer across different AI providers

For the complete decision rationale, see [ADR-008: Spring AI Integration](../adr/0008-spring-ai-integration.md).

## Architecture Overview

The chatbot architecture consists of several layers:

```
┌─────────────────────────────────────────┐
│         Angular Frontend                │
│   (ai-panel component + services)       │
└─────────────┬───────────────────────────┘
              │ HTTP + SSE
┌─────────────▼───────────────────────────┐
│      AiChatController (REST API)        │
│  - /api/ai/chat                         │
│  - /api/ai/chat/stream                  │
│  - /api/ai/history/{conversationId}     │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│      AiChatService (Business Logic)     │
│  - Conversation management              │
│  - System prompt application            │
│  - Tool/function orchestration          │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│         ChatClient (Spring AI)          │
│  - Google Gemini ChatModel              │
│  - OpenAI ChatModel                     │
│  - InMemoryChatMemory                   │
└─────────────────────────────────────────┘
```

## Implementation Details

### 1. Maven Dependencies

First, add Spring AI dependencies to `pom.xml`:

```xml
<dependencies>
    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>1.1.0</version>
    </dependency>
    
    <!-- Google Gemini Support -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-vertex-ai-gemini-spring-boot-starter</artifactId>
        <version>1.1.0</version>
    </dependency>
    
    <!-- PGVector for RAG -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

### 2. ChatClient Configuration

Create `ChatClient` beans for each LLM provider:

```java
@Configuration
public class AiChatConfig {

    @Bean
    public ChatClient geminiChatClient(
            @Qualifier("geminiChatModel") ChatModel chatModel,
            ChatMemory chatMemory
    ) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            ChatMemory chatMemory
    ) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public InMemoryChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
```

**Key Points:**
- Each `ChatClient` is configured with a specific `ChatModel` provider
- `MessageChatMemoryAdvisor` automatically manages conversation history
- `InMemoryChatMemory` stores conversations (will migrate to PostgreSQL for persistence)

### 3. System Prompts and Guardrails

System prompts define the chatbot's behavior and constraints:

```java
public class SystemPrompts {

    public static final String ASSISTANT_INSTRUCTIONS = """
        You are an AI assistant for Michael Kopp's portfolio website (mkopp.dev).
        
        Your role:
        - Answer questions about Michael's projects, skills, and experience
        - Provide technical insights into the portfolio architecture
        - Guide visitors through the website features
        
        Guidelines:
        - Be professional but conversational
        - Reference specific projects and technologies when relevant
        - If unsure, acknowledge limitations honestly
        - Do not make up information about Michael's background
        - Keep responses concise (2-3 paragraphs maximum)
        
        Available context:
        - Frontend: Angular 20 with SSR, TailwindCSS, DaisyUI
        - Backend: Spring Boot 3.5, Spring Modulith, PostgreSQL 16
        - AI: Spring AI 1.1.0 with Gemini/OpenAI support
        - DevOps: Docker, Traefik, GitHub Actions
        """;

    public static final String BLOG_POST_GENERATOR_INSTRUCTIONS = """
        You are a technical blog post generator for Michael Kopp's portfolio.
        
        Your role:
        - Generate technical blog posts based on provided topics
        - Use Markdown formatting with code examples
        - Include proper headings, lists, and code blocks
        
        Guidelines:
        - Write in first person (as Michael)
        - Include practical examples and code snippets
        - Explain complex concepts clearly
        - Use proper Markdown syntax for highlight.js compatibility
        """;
}
```

**Guardrails:**
- Restrict responses to portfolio-related topics
- Prevent hallucination by acknowledging limitations
- Enforce response length limits
- Maintain professional tone

### 4. REST API Endpoints

The `AiChatController` exposes chat endpoints:

```java
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    // Standard chat (blocking)
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return aiChatService.chat(
            request.message(), 
            request.conversationId(), 
            request.modelType()
        );
    }

    // Streaming chat (SSE)
    @PostMapping("/chat/stream")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return aiChatService.chatStream(
            request.message(), 
            request.conversationId(), 
            request.modelType()
        );
    }

    // Retrieve conversation history
    @GetMapping("/history/{conversationId}")
    public List<Message> getHistory(@PathVariable String conversationId) {
        return aiChatService.getConversationHistory(conversationId);
    }
}
```

**Features:**
- `/chat`: Synchronous responses for simple queries
- `/chat/stream`: Server-Sent Events (SSE) for real-time streaming
- `/history/{conversationId}`: Retrieve past conversation messages

### 5. Function Calling (Tools)

Spring AI supports function calling for dynamic tool usage:

```java
@Component
public class DateTimeTools {

    @Tool(description = "Get the current date and time in ISO-8601 format")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "Get the current date in YYYY-MM-DD format")
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }
}
```

Register tools with the `ChatClient`:

```java
@Bean
public ChatClient geminiChatClient(
        ChatModel chatModel,
        ChatMemory chatMemory,
        DateTimeTools dateTimeTools
) {
    return ChatClient.builder(chatModel)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(chatMemory),
                new FunctionCallingAdvisor(dateTimeTools)
            )
            .build();
}
```

**Use Case:**
When a user asks "What's today's date?", the LLM recognizes it needs the `getCurrentDate()` tool, calls it, and incorporates the result into the response.

### 6. RAG Architecture (Skeleton)

The RAG implementation uses PGVector for vector storage:

```java
@Configuration
public class RagConfig {

    @Bean
    public SimpleVectorStore vectorStore(
            DataSource dataSource,
            EmbeddingModel embeddingModel
    ) {
        return SimpleVectorStore.builder()
                .dataSource(dataSource)
                .embeddingModel(embeddingModel)
                .tableName("vector_embeddings")
                .build();
    }
}
```

**Planned Enhancement:**
- Vectorize entire codebase for code-aware responses
- Embed Spring Boot documentation for technical guidance
- Support document upload for context expansion

### 7. Frontend Integration

The Angular frontend uses the generated OpenAPI client:

```typescript
@Component({
  selector: 'app-ai-panel',
  templateUrl: './ai-panel.component.html',
  styleUrls: ['./ai-panel.component.scss']
})
export class AiPanelComponent {
  private aiService = inject(AiChatControllerService);
  
  selectedModel = signal<ModelType>('GEMINI');
  conversationId = signal<string>(crypto.randomUUID());
  messages = signal<ChatMessage[]>([]);

  sendMessage(userMessage: string): void {
    // Add user message to UI
    this.messages.update(msgs => [...msgs, {
      role: 'user',
      content: userMessage
    }]);

    // Stream AI response
    this.aiService.chatStream({
      message: userMessage,
      conversationId: this.conversationId(),
      modelType: this.selectedModel()
    }).subscribe({
      next: (chunk) => {
        // Append streaming chunks to assistant message
        this.appendToLastMessage(chunk);
      },
      error: (err) => console.error('Chat error:', err)
    });
  }
}
```

**Features:**
- Model selection dropdown (Gemini/OpenAI)
- Streaming response rendering
- Conversation ID persistence for history retrieval
- Mobile-responsive UI with DaisyUI components

## Challenges and Solutions

### Challenge 1: Memory Management
**Problem:** InMemoryChatMemory is lost on server restart.

**Solution:** Planned migration to PostgreSQL-backed chat memory using a custom `ChatMemory` implementation.

### Challenge 2: Token Limits
**Problem:** Long conversations exceed context window limits.

**Solution:** Implement conversation summarization and sliding window memory retention.

### Challenge 3: Streaming UI State
**Problem:** Managing streaming chunks in Angular signals.

**Solution:** Use RxJS to buffer chunks and update UI incrementally:

```typescript
this.aiService.chatStream(request).pipe(
  scan((acc, chunk) => acc + chunk, ''),
  debounceTime(50) // Smooth rendering
).subscribe(fullMessage => {
  this.updateLastMessage(fullMessage);
});
```

## Performance Considerations

- **Streaming vs. Blocking:** Streaming provides better UX for long responses (perceived performance improvement)
- **Caching:** Future: cache common queries (e.g., "What technologies are used?")
- **Rate Limiting:** Implement per-user rate limits to prevent abuse
- **Cost Management:** Monitor API usage with Google Cloud/OpenAI billing alerts

## Future Enhancements

1. **Persistent Chat Memory:** Migrate from `InMemoryChatMemory` to PostgreSQL
2. **RAG Implementation:** Vectorize codebase and documentation
3. **Multi-Modal Support:** Image analysis and structured output
4. **Advanced Tools:** GitHub API integration, code execution sandbox
5. **Conversation Summarization:** Automatic summarization for long conversations
6. **Analytics:** Track popular questions and user satisfaction

## Conclusion

Spring AI provides a powerful, Spring-native way to integrate AI capabilities into Java applications. The framework's abstraction layer makes it easy to switch between providers, and the built-in support for chat memory, function calling, and RAG makes advanced features accessible.

The current implementation serves as a foundation for more sophisticated AI features. The architecture is designed to scale from simple Q&A to a fully context-aware AI assistant for the portfolio.

## Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [ADR-008: Spring AI Integration](../adr/0008-spring-ai-integration.md)
- [Google Gemini API](https://ai.google.dev/)
- [OpenAPI 3.1 Contract](../../api-contracts/backend/openapi.json)
- [Source Code: AiChatConfig.java](../../apps/mysite-backend/src/main/java/dev/mkopp/mysite/ai/chat/config/AiChatConfig.java)

---

**Next Post:** Building a Blog System with PostgreSQL, Flyway, and SSR
