# Notia RAG Chat Setup

## Overview
The Chat feature now uses your notes stored in ChromaDB as a knowledge base. When you save notes in the application, they are automatically stored in the vector database for RAG (Retrieval-Augmented Generation).

## Prerequisites

1. **ChromaDB Running**: Make sure ChromaDB is running on `http://[::1]:8000`
   - You can start it with: `chroma run --host :: --port 8000`

2. **Gemini API Key**: Set your Gemini API key as an environment variable:
   ```powershell
   $env:GEMINI_API_KEY="your-api-key-here"
   ```

## How It Works

### Automatic Note Storage
- When you create or update a note in the Notia application, it is automatically stored in ChromaDB
- The note is embedded with title, ID, and content
- Collection name: `notia-notes-collection`

### Using the Chat Feature

To run the chat interface:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.notia.Chat"
```

The chat assistant will:
- Connect to your existing ChromaDB instance
- Retrieve the most relevant notes based on your query
- Use Google Gemini 1.5 Flash to answer questions about your notes
- Remember conversation history (last 10 messages)

### Example Queries
- "What notes do I have about [topic]?"
- "Summarize my notes on [subject]"
- "Find information about [keyword]"

## Configuration

### ChromaDB Settings (in VectorDB.java)
- **URL**: `http://[::1]:8000`
- **Collection**: `notia-notes-collection`
- **Embedding Model**: AllMiniLmL6V2EmbeddingModel

### Chat Settings (in Chat.java)
- **LLM**: Google Gemini 1.5 Flash
- **Max Results**: 2 most relevant notes per query
- **Min Score**: 0.5 (similarity threshold)
- **Chat Memory**: 10 messages

## Troubleshooting

**Error: Could not connect to ChromaDB**
- Ensure ChromaDB is running: `chroma run --host :: --port 8000`
- Check if port 8000 is available

**Error: Gemini API Key not found**
- Set the environment variable before running the chat:
  ```powershell
  $env:GEMINI_API_KEY="your-api-key-here"
  ```

**No relevant notes found**
- Make sure you have saved some notes in the application first
- The notes need to have content (not just titles)
- Try more specific queries

## Architecture

1. **Database.java**: Handles SQL database + automatic vector storage
2. **VectorDB.java**: Wrapper for ChromaDB operations
3. **Chat.java**: RAG implementation using stored notes with Gemini
4. **Assistant.java**: AI service interface for chat

Notes are stored in both:
- H2 Database (SQL) - for structured data and queries
- ChromaDB (Vector) - for semantic search and RAG
