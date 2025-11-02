# Delete Functionality - Notes and Embeddings

## Overview
When you delete a note in Notia, it is now properly removed from both the SQL database (H2) and the vector database (ChromaDB), along with all associated categories and tags.

## How It Works

### Before (Issue)
Previously, when a note was deleted:
- ❌ Foreign key constraints prevented deletion
- ❌ Note remained in database
- ❌ Delete button didn't work

### After (Fixed)
Now when a note is deleted:
- ✅ Associated categories removed
- ✅ Associated tags removed  
- ✅ Note deleted from H2 database
- ✅ Embedding deleted from ChromaDB
- ✅ Note completely removed from AI knowledge base

## Technical Implementation

### 1. Cascading Delete
The delete operation now happens in the correct order:

```java
public static void deleteNote(int id) {
    // 1. Delete from note_categories table
    DELETE FROM note_categories WHERE note_id = ?
    
    // 2. Delete from note_tags table
    DELETE FROM note_tags WHERE note_id = ?
    
    // 3. Delete the note itself
    DELETE FROM notes WHERE id = ?
    
    // 4. Delete from vector database
    deleteNoteFromVectorDB(id)
}
```

### 2. Transaction Safety
All SQL deletes happen in a single transaction:
- If any step fails, all changes are rolled back
- Ensures database consistency
- Prevents partial deletions

### 3. Embedding ID System
Each note's embedding is stored with a unique ID:
```
note_[noteId]
```

Examples:
- Note ID 1 → Embedding ID: `note_1`
- Note ID 42 → Embedding ID: `note_42`
- Note ID 123 → Embedding ID: `note_123`

### 2. VectorDB Changes

**New Methods:**
```java
// Add text with a specific ID (for tracking)
public String addTextWithId(String text, String id)

// Remove single embedding by ID
public void removeById(String embeddingId)

// Remove multiple embeddings by IDs
public void removeAll(List<String> embeddingIds)
```

### 3. Database Changes

**Updated `storeNoteInVectorDB()`:**
```java
private static void storeNoteInVectorDB(int noteId, Note note) {
    String embeddingId = "note_" + noteId;
    vectorDB.addTextWithId(noteText, embeddingId);
}
```

**Updated `deleteNote()`:**
```java
public static void deleteNote(int id) {
    // Delete from SQL database
    String sql = "DELETE FROM notes WHERE id = ?";
    pstmt.setInt(1, id);
    pstmt.executeUpdate();
    
    // Delete from vector database
    deleteNoteFromVectorDB(id);
}
```

**New `deleteNoteFromVectorDB()`:**
```java
private static void deleteNoteFromVectorDB(int noteId) {
    String embeddingId = "note_" + noteId;
    vectorDB.removeById(embeddingId);
}
```

## Testing the Delete Functionality

### Test Steps:

1. **Create a test note:**
   ```
   Title: Test Note for Deletion
   Content: This is a test note about pizza recipes. 🍕
   ```

2. **Save the note** - It will be stored in both databases

3. **Test AI Chat:**
   - Open AI Chat sidebar
   - Ask: "What notes do I have about pizza?"
   - The AI should mention your test note

4. **Delete the note:**
   - Select the note
   - Click "Delete Note" button

5. **Verify deletion:**
   - Check that the note is gone from the list
   - Open AI Chat
   - Ask again: "What notes do I have about pizza?"
   - The AI should say it doesn't find relevant notes

### Console Output
When deleting a note, you should see:
```
Deleted note [id] from vector database
```

If there's an issue, you'll see:
```
Warning: Failed to delete note from vector database: [error message]
```

## Edge Cases Handled

### 1. VectorDB Connection Failure
If ChromaDB is not running:
- Note is still deleted from SQL database
- Warning message logged
- Application continues to function

### 2. Duplicate Deletions
Attempting to delete the same note twice:
- First deletion: Success
- Second deletion: No error (graceful handling)

### 3. Missing Embeddings
If embedding doesn't exist:
- ChromaDB handles gracefully
- Warning logged but no crash

## Database Consistency

### On Note Save:
```
SQL Database    →  INSERT/UPDATE note
Vector Database →  ADD/UPDATE embedding with ID "note_[id]"
```

### On Note Update:
```
SQL Database    →  UPDATE note content
Vector Database →  REPLACE embedding (same ID)
```

### On Note Delete:
```
SQL Database    →  DELETE note
Vector Database →  REMOVE embedding by ID "note_[id]"
```

## Troubleshooting

### "Note deleted from list but still appears in AI chat"

**Possible causes:**
1. ChromaDB not running when note was deleted
2. Network issue during deletion
3. Embedding ID mismatch

**Solution:**
```powershell
# Restart ChromaDB
chroma run --host :: --port 8000

# Restart the application
mvn javafx:run
```

### "Cannot delete note - error message"

**Check:**
1. Is ChromaDB running?
2. Check console for specific error messages
3. Verify database file permissions

### "Old notes still showing in AI responses"

**Fix:**
1. Clear ChromaDB collection:
   - Stop ChromaDB
   - Delete `chroma_data` folder
   - Restart ChromaDB
   - Re-save all notes (they will be re-embedded)

## Migration Note

### Existing Notes
Notes created **before** this update:
- Were stored without IDs
- Will be updated with IDs on next save/update
- May need manual cleanup if deleted previously

### Fresh Start (Recommended)
For best results:
1. Export your notes (copy content)
2. Delete `chroma_data` folder
3. Restart ChromaDB
4. Restart application
5. Re-create notes

This ensures all embeddings have proper IDs.

## Benefits

✅ **Cleaner AI Responses** - Only current notes are referenced

✅ **Better Privacy** - Deleted notes truly deleted

✅ **Database Consistency** - SQL and Vector DBs stay in sync

✅ **Disk Space** - Old embeddings don't accumulate

✅ **Performance** - Fewer embeddings to search through

## Future Improvements

Potential enhancements:
- Bulk delete with progress indicator
- Soft delete with restore option
- Deletion history/audit log
- Automatic cleanup on startup

## Code References

**Files Modified:**
- `VectorDB.java` - Added delete methods
- `Database.java` - Updated save/delete to use IDs

**Key Methods:**
- `VectorDB.addTextWithId()` - Store with ID
- `VectorDB.removeById()` - Delete by ID
- `Database.deleteNoteFromVectorDB()` - Helper method
- `Database.storeNoteInVectorDB()` - Updated to use IDs
