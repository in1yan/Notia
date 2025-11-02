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

public class Chat {

    /**
     * This example demonstrates how to implement a naive Retrieval-Augmented Generation (RAG) application.
     * By "naive", we mean that we won't use any advanced RAG techniques.
     * In each interaction with the Large Language Model (LLM), we will:
     * 1. Take the user's query as-is.
     * 2. Embed it using an embedding model.
     * 3. Use the query's embedding to search an embedding store (containing small segments of your documents)
     * for the X most relevant segments.
     * 4. Append the found segments to the user's query.
     * 5. Send the combined input (user query + segments) to the LLM.
     * 6. Hope that:
     * - The user's query is well-formulated and contains all necessary details for retrieval.
     * - The found segments are relevant to the user's query.
     */

    public static void main(String[] args) {

        // Let's create an assistant that will use our notes stored in ChromaDB
        Assistant assistant = createAssistant();

        // Now, let's start the conversation with the assistant.
        // The assistant can answer questions based on all notes stored in the vector database.
        startConversationWith(assistant);
    }

    private static Assistant createAssistant() {

        // Customizable settings for the RAG system
        final double TEMPERATURE = 0.7; // Controls randomness: 0.0 = deterministic, 1.0 = creative
        final int MAX_RESULTS = 3; // Number of relevant note segments to retrieve
        final double MIN_SCORE = 0.5; // Minimum similarity threshold
        
        final String SYSTEM_PROMPT = 
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

        // First, let's create a chat model, also known as a LLM, which will answer our queries.
        // We are using Google's Gemini model for this application.
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_API_KEY)
                .modelName("gemini-1.5-flash")
                .temperature(TEMPERATURE)
                .build();


        // Create the embedding model that matches what's used in VectorDB.java
        // This ensures compatibility with the notes stored in ChromaDB.
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();


        // Connect to the existing ChromaDB instance where notes are stored.
        // This uses the same ChromaDB instance and collection managed by Database.java.
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .apiVersion(V2)
                .baseUrl("http://[::1]:8000")
                .collectionName("notia-notes-collection")
                .build();


        // The content retriever is responsible for retrieving relevant content based on a user query.
        // Currently, it is capable of retrieving text segments, but future enhancements will include support for
        // additional modalities like images, audio, and more.
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(MAX_RESULTS) // on each interaction we will retrieve the most relevant segments
                .minScore(MIN_SCORE) // we want to retrieve segments at least somewhat similar to user query
                .build();


        // Optionally, we can use a chat memory, enabling back-and-forth conversation with the LLM
        // and allowing it to remember previous interactions.
        // Currently, LangChain4j offers two chat memory implementations:
        // MessageWindowChatMemory and TokenWindowChatMemory.
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);


        // The final step is to build our AI Service,
        // configuring it to use the components we've created above.
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
    }
}
