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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Initialize VectorDB connection for RAG functionality
        initializeVectorDB();
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
        if (vectorDB != null && note.getContent() != null && !note.getContent().trim().isEmpty()) {
            try {
                String noteText = "Note ID: " + noteId + "\nTitle: " + note.getTitle() + "\n\n" + note.getContent();
                String embeddingId = "note_" + noteId;
                vectorDB.addTextWithId(noteText, embeddingId);
            } catch (Exception e) {
                System.err.println("Warning: Failed to store note in vector database: " + e.getMessage());
            }
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