package com.notia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:h2:./notia_db";

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS notes (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "title VARCHAR(255) NOT NULL," +
                         "content TEXT NOT NULL)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
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
        String sql = "SELECT id, title, content FROM notes WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Note(rs.getInt("id"), rs.getString("title"), rs.getString("content"));
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
        String sql = "INSERT INTO notes(title, content) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
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

    private static int updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setInt(3, note.getId());
            pstmt.executeUpdate();
            return note.getId();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void deleteNote(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
