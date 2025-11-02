# Chat Error: "Text Segment Cannot Be Null"

## What This Error Means

This error occurs when the AI chatbot tries to search your notes in ChromaDB, but encounters a problem with empty or null note content.

## Common Causes

1. **No Notes in Database** - You haven't created any notes yet
2. **Empty Notes** - Notes exist but have no content
3. **ChromaDB Not Running** - The vector database isn't available
4. **Notes Not Saved to VectorDB** - Notes in SQL but not in ChromaDB

## How to Fix

### Solution 1: Create and Save Notes

1. Click "✚ New" to create a note
2. Type some content (not just a title)
3. Click "💾 Save" to save the note
4. Wait a few seconds for it to be indexed
5. Try the chat again

**Example Note:**
```markdown
# My First Note

This is a test note about artificial intelligence.
AI is transforming how we work and interact with technology.
```

### Solution 2: Ensure ChromaDB is Running

```powershell
# Start ChromaDB
chroma run --host :: --port 8000
```

Make sure you see:
```
Running Chroma
```

### Solution 3: Restart the Application

Sometimes the connection needs to be refreshed:

1. Close Notia
2. Make sure ChromaDB is running
3. Restart Notia with: `mvn javafx:run`

### Solution 4: Re-index All Notes

If you have notes but they're not searchable:

1. Edit an existing note
2. Make a small change
3. Click "💾 Save"
4. This will re-add it to the vector database

## Verification Steps

### Check Console Output

When you save a note, you should see:
```
Successfully stored note [ID] in vector database
```

If you see warnings:
```
Warning: Note [ID] has no content, skipping vector storage
```
This means the note is empty - add content and save again.

### Test Query

Once you have notes saved:

1. Open AI Chat (💬 AI Chat button)
2. Ask: "What notes do I have?"
3. The AI should respond with information from your notes

## Error Messages Explained

### "No notes found. Please create and save some notes first"
- **Cause**: VectorDB is empty
- **Fix**: Create and save notes with content

### "Warning: VectorDB not initialized"
- **Cause**: ChromaDB connection failed
- **Fix**: Start ChromaDB and restart the app

### "Warning: Cannot add null or empty text to vector database"
- **Cause**: Trying to save a note without content
- **Fix**: Add content to your note before saving

### "text segment cannot be null"
- **Cause**: Search query returned null results
- **Fix**: Make sure notes are saved and indexed

## Prevention

To avoid this error in the future:

1. **Always add content** - Don't save empty notes
2. **Keep ChromaDB running** - Start it before using the app
3. **Save notes properly** - Use the 💾 Save button
4. **Wait for confirmation** - Check console for success messages

## Quick Setup Guide

For first-time setup:

```powershell
# Terminal 1: Start ChromaDB
chroma run --host :: --port 8000

# Terminal 2: Set API key and run app
$env:GEMINI_API_KEY="your-api-key-here"
mvn javafx:run
```

Then in the app:
1. Create a note with content
2. Save it (💾 Save)
3. Wait for "Successfully stored note X in vector database"
4. Open AI Chat (💬 AI Chat)
5. Ask a question about your notes

## Still Having Issues?

Check the console output for specific error messages:

```powershell
# Run with full error details
mvn javafx:run 2>&1 | Tee-Object -FilePath error.log
```

Look for:
- Connection errors to ChromaDB
- API key issues
- Embedding model errors
- Database write failures

## Debug Checklist

- [ ] ChromaDB is running on port 8000
- [ ] GEMINI_API_KEY is set
- [ ] At least one note exists with content
- [ ] Note has been saved (not just created)
- [ ] Console shows "Successfully stored note..." message
- [ ] AI Chat sidebar is open
- [ ] No other error messages in console

If all checkboxes are ticked and it still doesn't work, check the full error stack trace in the console.
