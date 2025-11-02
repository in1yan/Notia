# Academic Report: Notia - An AI-Powered Note-Taking Application

## ABSTRACT

Notia is a modern, desktop-based note-taking application designed to seamlessly integrate advanced AI capabilities into the user's workflow. The project's core is a robust markdown editor coupled with a sophisticated organization system using categories and tags. What sets Notia apart is its AI chat assistant, powered by the Gemini-2.5-pro model, which allows users to converse with their notes. This is achieved through a Retrieval-Augmented Generation (RAG) architecture, where notes are stored and indexed in a ChromaDB vector database, enabling semantic search and context-aware responses from the AI. The application is built using Java and the JavaFX framework for the user interface, with a MySQL database for storing note content and metadata. This report details the design, architecture, and implementation of the Notia application, showcasing a practical implementation of a DBMS project with integrated AI.

## ACKNOWLEDGEMENT

We would like to express our sincere gratitude to our project guide for their invaluable guidance and support throughout the development of this project. We would also like to thank our peers for their constructive feedback and encouragement. Finally, we are grateful for the open-source community and the developers of the libraries and frameworks that made this project possible.

---

## Chapter 1: INTRODUCTION

### 1.1 INTRODUCTION

In the information age, the ability to efficiently capture, organize, and retrieve information is paramount. Note-taking applications have become essential tools for students, professionals, and creatives alike. However, as the volume of notes grows, traditional methods of organization and search can become cumbersome and inefficient. Notia is a project that addresses this challenge by leveraging the power of Artificial Intelligence to create a more intuitive and powerful note-taking experience. It is a desktop application built with Java, featuring a clean, modern user interface and a powerful set of features designed to enhance productivity.

### 1.2 SCOPE OF THE WORK

The scope of this project is to develop a fully functional desktop note-taking application with the following key features:
- A markdown editor with real-time preview.
- The ability to create, read, update, and delete notes.
- A system for organizing notes using categories and tags.
- An AI-powered chat assistant that can answer questions based on the user's notes.
- A backend system consisting of a relational database (MySQL) for structured data and a vector database (ChromaDB) for unstructured text data to support semantic search.

### 1.3 PROBLEM STATEMENT

The primary problem this project aims to solve is the difficulty of managing and retrieving information from a large collection of personal notes. Traditional keyword-based search can be limiting, as it often fails to capture the semantic meaning of the user's query. Users need a more natural and efficient way to interact with their knowledge base. Notia addresses this by implementing a conversational AI that understands the user's intent and retrieves the most relevant information from their notes.

### 1.4 AIM AND OBJECTIVES OF THE PROJECT

The main aim of the project is to design and implement an intelligent note-taking application that enhances user productivity.

The objectives are as follows:
- To develop a user-friendly interface using JavaFX.
- To implement a robust backend system for storing and managing notes using MySQL.
- To integrate a vector database (ChromaDB) for efficient similarity search.
- To build a RAG pipeline using LangChain4j and the Gemini-2.5-pro model.
- To provide a seamless and intuitive user experience.

---

## CHAPTER 2: SYSTEM SPECIFICATIONS

### HARDWARE SPECIFICATIONS

- **Processor:** Intel Core i3 or equivalent
- **RAM:** 4 GB or more
- **Hard Disk:** 1 GB of free space

### SOFTWARE SPECIFICATIONS

- **Operating System:** Windows, macOS, or Linux
- **Java Development Kit (JDK):** Version 17 or higher
- **Build Tool:** Apache Maven
- **Database:** MySQL 8.0 or higher
- **Vector Database:** ChromaDB
- **AI Model:** Google Gemini-2.5-pro
- **Libraries & Frameworks:**
    - JavaFX
    - LangChain4j
    - mysql-connector-j
    - commonmark

---

## CHAPTER 3: MODULE DESCRIPTION

The Notia application is designed with a modular architecture, with each module responsible for a specific set of functionalities.

### Note Management Module
This module handles the core CRUD (Create, Read, Update, Delete) operations for notes. It provides the functionality to create new notes, save them to the database, edit existing notes, and delete them.

### Organization Module
To help users organize their notes, this module implements a system of categories and tags. Users can assign one or more categories and tags to each note, and then filter their notes based on these assignments.

### Markdown Editor Module
This module provides a rich text editing experience using markdown syntax. It includes a text area for writing markdown and a web view for rendering a live preview of the formatted content.

### Database Module
The Database module is responsible for all interactions with the MySQL database. It handles the storage and retrieval of notes, categories, tags, and the relationships between them. It uses JDBC for database connectivity.

### Vector Database Module
This module interfaces with ChromaDB, the vector database used for storing note embeddings. When a note is saved or updated, its content is converted into a vector embedding and stored in ChromaDB. This enables efficient semantic search.

### AI Chat Module
The AI Chat module is the most innovative feature of Notia. It provides a conversational interface for users to interact with their notes. It uses a RAG (Retrieval-Augmented Generation) pipeline. When a user asks a question, the module queries the ChromaDB to find the most relevant notes, and then passes these notes as context to the Gemini-2.5-pro model to generate a response.

### GUI Module
The GUI module is built using JavaFX and is responsible for the application's user interface. It provides a clean, modern, and intuitive interface for users to interact with the application's features. It features a "Rosé Pine" color theme for a pleasant visual experience.

---

## CHAPTER 4: SAMPLE CODING

### RAG Implementation (`Chat.java`)

The following code snippet is from the `Chat.java` file and demonstrates the core logic of the RAG implementation.

```java
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

    private static Assistant createAssistant() {

        final double TEMPERATURE = 0.7;
        final int MAX_RESULTS = 3;
        final double MIN_SCORE = 0.5;
        
        final String SYSTEM_PROMPT = 
            "You are an intelligent note-taking assistant...";

        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_API_KEY)
                .modelName("gemini-1.5-flash")
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

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
    }
}
```

### Database Interaction (`Database.java`)

The following code from `Database.java` shows how the application connects to the MySQL database, creates the schema if it doesn't exist, and handles saving a note.

```java
package com.notia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/notia_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "content TEXT," +
                    "created_on DATE," +
                    "updated_on DATE," +
                    "is_embedded BOOLEAN," +
                    "is_subnote BOOLEAN," +
                    "parent_id INT)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL UNIQUE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tags (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL UNIQUE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS note_categories (" +
                    "note_id INT," +
                    "category_id INT," +
                    "PRIMARY KEY (note_id, category_id)," +
                    "FOREIGN KEY (note_id) REFERENCES notes(id)," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS note_tags (" +
                    "note_id INT," +
                    "tag_id INT," +
                    "PRIMARY KEY (note_id, tag_id)," +
                    "FOREIGN KEY (note_id) REFERENCES notes(id)," +
                    "FOREIGN KEY (tag_id) REFERENCES tags(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int saveNote(Note note) {
        if (note.getId() == 0) {
            return insertNote(note);
        } else {
            return updateNote(note);
        }
    }

    private static int insertNote(Note note) {
        String sql = "INSERT INTO notes(title, content, created_on, updated_on) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
```

---

## CHAPTER 5: DATABASE SCHEMA

The application utilizes a MySQL relational database to persist and manage user notes, categories, and tags. The schema is designed to be simple yet effective, ensuring data integrity and efficient querying.

### Entity-Relationship (ER) Diagram (Text-based)

```
+-----------+      +-------------------+      +------------+
|  notes    |      |  note_categories  |      | categories |
+-----------+      +-------------------+      +------------+
| id (PK)   |------<| note_id (FK)      |      | id (PK)    |
| title     |      | category_id (FK)  |>-----| name       |
| content   |      +-------------------+      +------------+
| ...       |
+-----------+
      |
      |
      |      +-------------------+      +------------+
      +------<|  note_tags        |      |    tags    |
             +-------------------+      +------------+
             | note_id (FK)      |      | id (PK)    |
             | tag_id (FK)       |>-----| name       |
             +-------------------+      +------------+
```

### Table Descriptions

#### `notes` table
This is the central table, storing the content of each note.

| Column | Type | Description |
|---|---|---|
| `id` | INT (PK) | Unique identifier for each note. |
| `title` | VARCHAR(255) | The title of the note, typically the first line of the content. |
| `content` | TEXT | The full markdown content of the note. |
| `created_on` | DATE | The date the note was created. |
| `updated_on` | DATE | The date the note was last updated. |
| `is_embedded`| BOOLEAN | Flag to indicate if the note has been embedded in the vector DB. |
| `is_subnote` | BOOLEAN | Flag to indicate if the note is a sub-note. |
| `parent_id` | INT (FK) | If it's a sub-note, this points to the parent note's ID. |

#### `categories` table
This table stores the user-defined categories.

| Column | Type | Description |
|---|---|---|
| `id` | INT (PK) | Unique identifier for each category. |
| `name` | VARCHAR(255) | The name of the category (e.g., "Work", "Personal"). |

#### `tags` table
This table stores the user-defined tags.

| Column | Type | Description |
|---|---|---|
| `id` | INT (PK) | Unique identifier for each tag. |
| `name` | VARCHAR(255) | The name of the tag (e.g., "important", "idea"). |

#### `note_categories` table
This is a junction table to manage the many-to-many relationship between notes and categories.

| Column | Type | Description |
|---|---|---|
| `note_id` | INT (FK) | Foreign key referencing the `notes` table. |
| `category_id` | INT (FK) | Foreign key referencing the `categories` table. |

#### `note_tags` table
This is a junction table to manage the many-to-many relationship between notes and tags.

| Column | Type | Description |
|---|---|---|
| `note_id` | INT (FK) | Foreign key referencing the `notes` table. |
| `tag_id` | INT (FK) | Foreign key referencing the `tags` table. |

---

## CHAPTER 6: CONCLUSION AND FUTURE ENHANCEMENT

### CONCLUSION

The Notia project successfully demonstrates the integration of AI into a traditional desktop application to create a more powerful and intuitive user experience. By combining a relational database for structured data and a vector database for semantic search, the application provides a robust solution for managing and retrieving information from a personal knowledge base. The use of JavaFX for the frontend and a modular backend design makes the application scalable and maintainable. The AI chat assistant, powered by the Gemini-2.5-pro model and a RAG pipeline, is a key feature that sets Notia apart from other note-taking applications.

### FUTURE ENHANCEMENT

While the current version of Notia is a fully functional application, there are several potential areas for future enhancement:
- **Cloud Sync:** Implement a cloud synchronization feature to allow users to access their notes from multiple devices.
- **Multi-modal Notes:** Extend the application to support not just text, but also images, audio, and other media types in notes.
- **Advanced RAG:** Implement more advanced RAG techniques, such as query transformation and document re-ranking, to improve the accuracy of the AI assistant.
- **Collaborative Features:** Add features to allow multiple users to collaborate on notes in real-time.
- **Plugin Architecture:** Develop a plugin architecture to allow third-party developers to extend the functionality of the application.