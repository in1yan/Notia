package com.notia;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import shared.Assistant;

import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V2;
import static shared.Utils.*;

public class ChatAssistantFactory {

    // Customizable RAG settings
    private static final double TEMPERATURE = 0.7; // Range: 0.0 (deterministic) to 1.0 (creative)
    private static final int MAX_RESULTS = 3; // Number of relevant notes to retrieve
    private static final double MIN_SCORE = 0.5; // Minimum similarity score (0.0 to 1.0)
    private static final int CHAT_MEMORY_SIZE = 10; // Number of messages to remember
    
    private static final String SYSTEM_PROMPT = 
        "You are an intelligent note-taking assistant integrated into the Notia app. " +
        "Your role is to help users find information in their personal notes and answer questions based on the content they've written. " +
        "\n\nGuidelines:" +
        "\n- Be concise and helpful in your responses" +
        "\n- Base your answers primarily on the user's notes provided in the context" +
        "\n- If the notes don't contain relevant information, clearly state that" +
        "\n- You can help summarize, find connections, and explain concepts from the notes" +
        "\n- Be friendly and conversational" +
        "\n- If you're uncertain, admit it rather than making up information" +
        "\n\nRemember: You're helping users interact with THEIR OWN notes, so treat the information as their personal knowledge base.";

    public static Assistant createAssistant() {
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_API_KEY)
                .modelName("gemini-2.5-pro")
                .temperature(TEMPERATURE)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .apiVersion(V2)
                .baseUrl("http://[::1]:8000")
                .collectionName("notia-notes-collection")
                .build();

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(CHAT_MEMORY_SIZE);

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
    }
    
    // Getter methods for configuration (useful for UI settings)
    public static double getTemperature() {
        return TEMPERATURE;
    }
    
    public static int getMaxResults() {
        return MAX_RESULTS;
    }
    
    public static double getMinScore() {
        return MIN_SCORE;
    }
    
    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}
