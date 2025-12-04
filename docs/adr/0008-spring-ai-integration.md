# 📄 ADR 008: Spring AI Integration for Chatbot

## Status

**Accepted** (2025-12-04)

## Context

The platform requires an AI-powered chatbot to:

* Assist visitors in exploring blog content and technical topics
* Demonstrate integration with modern AI/ML technologies
* Generate blog post content and provide portfolio assistance
* Showcase enterprise-level AI integration patterns

Key requirements:

* Support multiple LLM providers (Google Gemini, OpenAI, local models)
* Maintain conversation context and history
* Implement system prompts with guardrails
* Provide both streaming and non-streaming responses
* Enable future RAG (Retrieval Augmented Generation) capabilities
* Extensible tool/function calling

## Decision

We will use **Spring AI 1.1.0** as the AI integration framework.

Implementation details:

* **Multiple LLM Support**: Configure separate ChatClient beans for Gemini and OpenAI
* **Conversation Memory**: Use in-memory ChatMemory with conversation ID management
* **System Prompts**: Implement SystemPrompts class with predefined instructions and guardrails
* **Advisors**: Use MessageChatMemoryAdvisor for context retention
* **Vector Store**: Skeleton implementation with PGVector for future RAG
* **Tools**: Extensible function calling (DateTimeTools as example)
* **Streaming**: Support both Flux streaming and traditional responses
* **Frontend**: Responsive Angular chat component with mobile optimization

**Technology Stack:**
* Spring AI 1.1.0
* Google Gemini AI (primary)
* OpenAI (secondary)
* PostgreSQL with PGVector extension (for future vector storage)
* In-memory ChatMemory (temporary, planned migration to DB)

## Alternatives Considered

### LangChain4j

* ✅ Java-native AI framework
* ✅ Rich ecosystem and abstractions
* ❌ Adds another framework layer on top of Spring
* ❌ Less Spring-idiomatic
* ❌ Smaller community compared to Spring ecosystem

### Direct LLM API Integration

* ✅ Maximum control and flexibility
* ✅ No framework overhead
* ❌ Requires building abstractions from scratch
* ❌ No standardization across providers
* ❌ Difficult to switch LLM providers
* ❌ No built-in conversation memory, RAG, or tool calling

### Spring AI

* ✅ Official Spring project, Spring-idiomatic
* ✅ Unified API across multiple AI providers
* ✅ Built-in conversation memory and RAG support
* ✅ Easy provider switching
* ✅ Active development by Spring team
* ✅ Integrates seamlessly with existing Spring ecosystem
* ❌ Relatively new (though backed by VMware/Broadcom)

## Consequences

### Positive

* **Provider Flexibility**: Easy to switch between Gemini, OpenAI, or local LLMs
* **Spring Integration**: Leverages Spring dependency injection, configuration, and patterns
* **Feature Rich**: Built-in support for RAG, memory, tools, streaming
* **Future Proof**: Official Spring project with active development
* **Rapid Development**: High-level abstractions reduce boilerplate
* **Testability**: Spring beans are easily mockable for testing

### Negative

* **Framework Dependency**: Tied to Spring AI's abstractions and lifecycle
* **Early Stage**: Spring AI is relatively new (1.1.0), APIs may evolve
* **Memory Storage**: Current in-memory implementation needs migration to PostgreSQL
* **Learning Curve**: New framework requires learning Spring AI patterns

### Implementation Notes

**Current Features:**
* Basic chat with conversation history
* System prompts for portfolio assistant and blog post generator
* Tool usage (DateTimeTools example)
* Streaming and non-streaming responses
* Multiple LLM provider support

**Planned Enhancements:**
* Migrate chat memory from in-memory to PostgreSQL
* Complete RAG implementation with vectorized codebase
* Add vectorized Spring Boot and technology documentation
* Expand tool usage with more domain-specific functions
* Image analysis and multi-modal capabilities
* Structured output for blog post generation

## Related Documents

* [Design Document v0.4](../design.md)
* [Roadmap Phase 4: AI Integration](../roadmap.md#phase-4-ai-integration-completed)
* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
