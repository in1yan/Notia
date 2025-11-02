package com.notia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:h2:./notia_db";
    private static VectorDB vectorDB;

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
                    "parent_id INT," +
                    "FOREIGN KEY (parent_id) REFERENCES notes(id))");

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
            
            // Populate with example notes if database is empty
            populateExampleNotes();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Initialize VectorDB connection for RAG functionality
        initializeVectorDB();
    }
    
    private static void populateExampleNotes() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if database is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notes");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Database is empty. Populating with example notes...");
                
                // Create example notes about Notia
                createExampleNote(
                    "# Welcome to Notia! 🎉",
                    "# Welcome to Notia!\n\n" +
                    "Notia is a **powerful markdown note-taking application** with AI-powered search capabilities.\n\n" +
                    "## Key Features:\n" +
                    "- ✍️ **Markdown editing** with live preview\n" +
                    "- 🤖 **AI chatbot** powered by Google Gemini\n" +
                    "- 🔍 **Semantic search** using RAG (Retrieval Augmented Generation)\n" +
                    "- 🎨 **Beautiful Rosé Pine theme**\n" +
                    "- 📁 **Categories and tags** for organization\n" +
                    "- 😊 **Full emoji support**\n\n" +
                    "Start creating notes and ask the AI questions about them!"
                );
                
                createExampleNote(
                    "# Getting Started with Notia",
                    "# Getting Started with Notia\n\n" +
                    "## Creating Notes\n" +
                    "1. Click the **✚ New** button in the toolbar\n" +
                    "2. Write your content in **Markdown format**\n" +
                    "3. Click **💾 Save** to save your note\n\n" +
                    "## Viewing Notes\n" +
                    "- **👁 Preview**: See rendered markdown\n" +
                    "- **✏ Edit**: Edit the raw markdown\n" +
                    "- **⬌ Split**: See both at once\n\n" +
                    "## Organizing Notes\n" +
                    "Use the **☰ Sidebar** to:\n" +
                    "- Add categories to group related notes\n" +
                    "- Add tags for quick filtering\n" +
                    "- Click **➕ Add** below categories/tags\n\n" +
                    "## Deleting Notes\n" +
                    "Select a note and click **🗑 Delete** to remove it.\n" +
                    "The note will be deleted from both the database and AI search index."
                );
                
                createExampleNote(
                    "# AI Chat Assistant 💬",
                    "# AI Chat Assistant\n\n" +
                    "The AI chatbot helps you interact with your notes using natural language!\n\n" +
                    "## How to Use:\n" +
                    "1. Click **💬 AI Chat** button to open the sidebar\n" +
                    "2. Type your question in the chat box\n" +
                    "3. Press Send or hit Enter\n\n" +
                    "## What Can You Ask?\n" +
                    "- *\"What notes do I have about AI?\"*\n" +
                    "- *\"Summarize my notes on markdown\"*\n" +
                    "- *\"Find information about themes\"*\n" +
                    "- *\"What features does Notia have?\"*\n\n" +
                    "## How It Works:\n" +
                    "The AI uses **RAG (Retrieval Augmented Generation)**:\n" +
                    "1. Your notes are stored in **ChromaDB** as embeddings\n" +
                    "2. When you ask a question, relevant notes are retrieved\n" +
                    "3. **Google Gemini** generates responses based on your notes\n\n" +
                    "**Note**: Make sure ChromaDB is running on port 8000!"
                );
                
                createExampleNote(
                    "# Markdown Formatting Guide",
                    "# Markdown Formatting Guide\n\n" +
                    "Notia supports full **CommonMark** markdown syntax.\n\n" +
                    "## Text Formatting\n" +
                    "- **Bold text**: `**bold**` or `__bold__`\n" +
                    "- *Italic text*: `*italic*` or `_italic_`\n" +
                    "- `Inline code`: `` `code` ``\n" +
                    "- ~~Strikethrough~~: `~~text~~`\n\n" +
                    "## Headers\n" +
                    "```markdown\n" +
                    "# H1 Header\n" +
                    "## H2 Header\n" +
                    "### H3 Header\n" +
                    "```\n\n" +
                    "## Lists\n" +
                    "**Unordered:**\n" +
                    "- Item 1\n" +
                    "- Item 2\n" +
                    "  - Nested item\n\n" +
                    "**Ordered:**\n" +
                    "1. First item\n" +
                    "2. Second item\n\n" +
                    "## Code Blocks\n" +
                    "Use triple backticks:\n" +
                    "```java\n" +
                    "public static void main(String[] args) {\n" +
                    "    System.out.println(\"Hello Notia!\");\n" +
                    "}\n" +
                    "```\n\n" +
                    "## Quotes\n" +
                    "> This is a blockquote\n" +
                    "> It can span multiple lines\n\n" +
                    "## Links\n" +
                    "[Link text](https://example.com)\n\n" +
                    "## Emojis\n" +
                    "Full emoji support! 😊 🎉 💻 🚀 ✨"
                );
                
                createExampleNote(
                    "# Rosé Pine Theme 🌹",
                    "# Rosé Pine Theme\n\n" +
                    "Notia uses the beautiful **Rosé Pine** color scheme - a low-contrast dark theme.\n\n" +
                    "## Color Palette:\n" +
                    "- **Base**: `#191724` - Main background\n" +
                    "- **Surface**: `#1f1d2e` - Slightly lighter\n" +
                    "- **Overlay**: `#26233a` - Cards and elevated surfaces\n" +
                    "- **Text**: `#e0def4` - Primary text color\n\n" +
                    "## Accent Colors:\n" +
                    "- **Rose** `#ebbcba` - Primary accent (headings, highlights)\n" +
                    "- **Pine** `#31748f` - Interactive elements (buttons)\n" +
                    "- **Foam** `#9ccfd8` - Links and hover states\n" +
                    "- **Iris** `#c4a7e7` - Code syntax\n" +
                    "- **Gold** `#f6c177` - Warnings\n" +
                    "- **Love** `#eb6f92` - Errors\n\n" +
                    "## Design Principles:\n" +
                    "- **Low contrast** - Easy on the eyes for long sessions\n" +
                    "- **Warm colors** - Cozy and comfortable\n" +
                    "- **Clear hierarchy** - Good contrast where it matters\n\n" +
                    "The theme is applied throughout the entire application including the chat sidebar!"
                );
                
                createExampleNote(
                    "# Technical Stack 💻",
                    "# Technical Stack\n\n" +
                    "Notia is built with modern Java technologies.\n\n" +
                    "## Frontend:\n" +
                    "- **JavaFX 21** - Modern UI framework\n" +
                    "- **WebView** - For markdown rendering\n" +
                    "- **CSS** - Custom Rosé Pine styling\n\n" +
                    "## Backend:\n" +
                    "- **H2 Database** - Embedded SQL database for notes\n" +
                    "- **ChromaDB** - Vector database for embeddings\n" +
                    "- **Maven** - Build and dependency management\n\n" +
                    "## AI & ML:\n" +
                    "- **LangChain4j** - Java framework for LLMs\n" +
                    "- **Google Gemini 1.5 Flash** - Chat model\n" +
                    "- **AllMiniLM-L6-v2** - Embedding model (ONNX)\n" +
                    "- **RAG Pipeline** - Retrieval Augmented Generation\n\n" +
                    "## Markdown:\n" +
                    "- **CommonMark** - Parser and renderer\n" +
                    "- **GitHub Flavored Markdown** support\n\n" +
                    "## Setup Requirements:\n" +
                    "```bash\n" +
                    "# Install ChromaDB\n" +
                    "pip install chromadb\n\n" +
                    "# Run ChromaDB\n" +
                    "chroma run --host :: --port 8000\n\n" +
                    "# Set API key\n" +
                    "$env:GEMINI_API_KEY=\"your-key-here\"\n\n" +
                    "# Run Notia\n" +
                    "mvn javafx:run\n" +
                    "```"
                );
                
                System.out.println("Successfully created example notes!");
            }
        } catch (SQLException e) {
            System.err.println("Error populating example notes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createExampleNote(String title, String content) {
        Note note = new Note(0, title, content, 
            new java.sql.Date(System.currentTimeMillis()),
            new java.sql.Date(System.currentTimeMillis()),
            false, false, 0);
        saveNote(note);
    }

    private static void initializeVectorDB() {
        try {
            vectorDB = new VectorDB("http://[::1]:8000", "notia-notes-collection");
        } catch (Exception e) {
            System.err.println("Warning: Could not connect to ChromaDB. Vector search will be disabled.");
            System.err.println("Make sure ChromaDB is running on http://[::1]:8000");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT id, title FROM notes";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                notes.add(new Note(rs.getInt("id"), rs.getString("title")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public static Note getNoteById(int id) {
        String sql = "SELECT * FROM notes WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Note(rs.getInt("id"), rs.getString("title"), rs.getString("content"),
                        rs.getDate("created_on"), rs.getDate("updated_on"), rs.getBoolean("is_embedded"),
                        rs.getBoolean("is_subnote"), rs.getInt("parent_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int saveNote(Note note) {
        if (note.getId() == 0) {
            return insertNote(note);
        } else {
            return updateNote(note);
        }
    }

    private static int insertNote(Note note) {
        String sql = "INSERT INTO notes(title, content, created_on, updated_on, is_embedded, is_subnote, parent_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setBoolean(5, note.isEmbedded());
            pstmt.setBoolean(6, note.isSubnote());
            if (note.getParentId() == 0) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, note.getParentId());
            }
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int noteId = generatedKeys.getInt(1);
                        // Store note in vector database for RAG
                        storeNoteInVectorDB(noteId, note);
                        return noteId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ?, updated_on = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setInt(4, note.getId());
            pstmt.executeUpdate();
            // Update note in vector database for RAG
            storeNoteInVectorDB(note.getId(), note);
            return note.getId();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void storeNoteInVectorDB(int noteId, Note note) {
        if (vectorDB == null) {
            System.err.println("Warning: VectorDB not initialized");
            return;
        }
        
        if (note == null) {
            System.err.println("Warning: Cannot store null note in vector database");
            return;
        }
        
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            System.err.println("Warning: Note " + noteId + " has no content, skipping vector storage");
            return;
        }
        
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            System.err.println("Warning: Note " + noteId + " has no title, using 'Untitled'");
        }
        
        try {
            String title = (note.getTitle() != null && !note.getTitle().trim().isEmpty()) 
                          ? note.getTitle() : "Untitled";
            String noteText = "Note ID: " + noteId + "\nTitle: " + title + "\n\n" + note.getContent();
            String embeddingId = "note_" + noteId;
            vectorDB.addTextWithId(noteText, embeddingId);
            System.out.println("Successfully stored note " + noteId + " in vector database");
        } catch (Exception e) {
            System.err.println("Warning: Failed to store note " + noteId + " in vector database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteNote(int id) {
        try (Connection conn = getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Delete related records first
                String deleteCategoriesSql = "DELETE FROM note_categories WHERE note_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteCategoriesSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                
                String deleteTagsSql = "DELETE FROM note_tags WHERE note_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteTagsSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                
                // Delete the note itself
                String deleteNoteSql = "DELETE FROM notes WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteNoteSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                
                // Delete note from vector database
                deleteNoteFromVectorDB(id);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void deleteNoteFromVectorDB(int noteId) {
        if (vectorDB != null) {
            try {
                String embeddingId = "note_" + noteId;
                vectorDB.removeById(embeddingId);
                System.out.println("Deleted note " + noteId + " from vector database");
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete note from vector database: " + e.getMessage());
            }
        }
    }

    // Category Methods
    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static int addCategory(String name) {
        String sql = "INSERT INTO categories(name) VALUES(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            // Ignore unique constraint violation
        }
        return 0;
    }

    public static void updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setInt(2, category.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Category getCategoryByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tag Methods
    public static List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT * FROM tags";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public static int addTag(String name) {
        String sql = "INSERT INTO tags(name) VALUES(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            // Ignore unique constraint violation
        }
        return 0;
    }

    public static void updateTag(Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag.getName());
            pstmt.setInt(2, tag.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTag(int id) {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Tag getTagByName(String name) {
        String sql = "SELECT * FROM tags WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Note-Category and Note-Tag Methods
    public static void addCategoryToNote(int noteId, int categoryId) {
        String sql = "INSERT INTO note_categories(note_id, category_id) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            pstmt.setInt(2, categoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Ignore unique constraint violation
        }
    }

    public static void removeCategoryFromNote(int noteId, int categoryId) {
        String sql = "DELETE FROM note_categories WHERE note_id = ? AND category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            pstmt.setInt(2, categoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Category> getCategoriesForNote(int noteId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.id, c.name FROM categories c " +
                     "JOIN note_categories nc ON c.id = nc.category_id " +
                     "WHERE nc.note_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static void addTagToNote(int noteId, int tagId) {
        String sql = "INSERT INTO note_tags(note_id, tag_id) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            pstmt.setInt(2, tagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Ignore unique constraint violation
        }
    }

    public static void removeTagFromNote(int noteId, int tagId) {
        String sql = "DELETE FROM note_tags WHERE note_id = ? AND tag_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            pstmt.setInt(2, tagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Tag> getTagsForNote(int noteId) {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT t.id, t.name FROM tags t " +
                     "JOIN note_tags nt ON t.id = nt.tag_id " +
                     "WHERE nt.note_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public static List<Note> searchNotes(String searchText) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT DISTINCT n.id, n.title FROM notes n " +
                     "LEFT JOIN note_categories nc ON n.id = nc.note_id " +
                     "LEFT JOIN categories c ON nc.category_id = c.id " +
                     "LEFT JOIN note_tags nt ON n.id = nt.note_id " +
                     "LEFT JOIN tags t ON nt.tag_id = t.id " +
                     "WHERE n.title LIKE ? OR n.content LIKE ? OR c.name LIKE ? OR t.name LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(new Note(rs.getInt("id"), rs.getString("title")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public static List<Note> getNotesByCategory(int categoryId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.id, n.title FROM notes n " +
                     "JOIN note_categories nc ON n.id = nc.note_id " +
                     "WHERE nc.category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(new Note(rs.getInt("id"), rs.getString("title")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public static List<Note> getNotesByTag(int tagId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.id, n.title FROM notes n " +
                     "JOIN note_tags nt ON n.id = nt.note_id " +
                     "WHERE nt.tag_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tagId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(new Note(rs.getInt("id"), rs.getString("title")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }
}