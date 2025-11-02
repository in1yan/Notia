package com.notia;

import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V2;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import java.util.List;

public class VectorDB {
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public VectorDB(String chromaUrl, String collectionName) {
        this(chromaUrl, collectionName, false);
    }

    public VectorDB(String chromaUrl, String collectionName, boolean logRequests) {
        this.embeddingStore = ChromaEmbeddingStore.builder()
            .apiVersion(V2)
            .baseUrl(chromaUrl)
            .collectionName(collectionName)
            .logRequests(logRequests)
            .logResponses(logRequests)
            .build();
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    public void addText(String text) {
        TextSegment segment = TextSegment.from(text);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    public String addTextWithId(String text, String id) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Warning: Cannot add null or empty text to vector database");
            return null;
        }
        try {
            TextSegment segment = TextSegment.from(text);
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(id, embedding);
            return id;
        } catch (Exception e) {
            System.err.println("Error adding text to vector database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void addTexts(List<String> texts) {
        for (String text : texts) {
            addText(text);
        }
    }

    public void removeById(String embeddingId) {
        try {
            embeddingStore.remove(embeddingId);
        } catch (Exception e) {
            System.err.println("Warning: Failed to remove embedding from vector database: " + e.getMessage());
        }
    }

    public void removeAll(List<String> embeddingIds) {
        try {
            embeddingStore.removeAll(embeddingIds);
        } catch (Exception e) {
            System.err.println("Warning: Failed to remove embeddings from vector database: " + e.getMessage());
        }
    }

    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults)
            .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
        return result.matches();
    }

    public EmbeddingMatch<TextSegment> searchTopMatch(String query) {
        List<EmbeddingMatch<TextSegment>> matches = search(query, 1);
        return matches.isEmpty() ? null : matches.get(0);
    }

    public static void main(String[] args) {
        VectorDB vectorDB = new VectorDB("http://[::1]:8000", "test-collection", true);
        
        vectorDB.addText("I like football.");
        vectorDB.addText("The weather is good today.");

        EmbeddingMatch<TextSegment> match = vectorDB.searchTopMatch("What is your favourite sport?");
        
        if (match != null) {
            System.out.println(match.score());
            System.out.println(match.embedded().text());
        }
    }
}
