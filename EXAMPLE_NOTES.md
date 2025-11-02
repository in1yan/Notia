# Example Notes in Notia

## Overview

When you first run Notia, if the database is empty, it automatically populates with **6 example notes** that explain how the application works.

## Example Notes Included

### 1. Welcome to Notia! 🎉
An introduction note covering:
- What Notia is
- Key features overview
- Quick feature list with emojis

### 2. Getting Started with Notia
A practical guide including:
- How to create notes
- Different view modes (Preview, Edit, Split)
- Organizing with categories and tags
- Deleting notes

### 3. AI Chat Assistant 💬
Everything about the AI chatbot:
- How to use the chat
- Example questions to ask
- How RAG works behind the scenes
- ChromaDB requirements

### 4. Markdown Formatting Guide
Complete markdown reference:
- Text formatting (bold, italic, code)
- Headers and structure
- Lists (ordered and unordered)
- Code blocks with syntax highlighting
- Blockquotes and links
- Emoji support

### 5. Rosé Pine Theme 🌹
Design system documentation:
- Complete color palette with hex codes
- Accent colors and their uses
- Design principles
- Low-contrast dark theme benefits

### 6. Technical Stack 💻
Developer-focused information:
- Frontend technologies (JavaFX, WebView)
- Backend (H2, ChromaDB)
- AI/ML stack (LangChain4j, Gemini)
- Setup requirements with code snippets

## How It Works

The notes are created automatically when:
1. You first launch Notia
2. The `notes` table in H2 database is empty
3. During the `Database.initialize()` process

### Code Flow:
```java
Database.initialize()
  → populateExampleNotes()
    → Check if notes table is empty
    → If empty: create 6 example notes
    → Save and embed each note
```

## What Gets Created

For each example note:
- ✅ Note saved to H2 database
- ✅ Embedding created and stored in ChromaDB
- ✅ Creation and update timestamps set
- ✅ Ready for AI search immediately

## Console Output

When notes are created, you'll see:
```
Database is empty. Populating with example notes...
Successfully stored note 1 in vector database
Successfully stored note 2 in vector database
...
Successfully created example notes!
```

## Testing the AI with Examples

Once the notes are loaded, try asking:

**General Questions:**
- "What is Notia?"
- "What features does this app have?"
- "How do I use the AI chat?"

**Technical Questions:**
- "What technologies are used?"
- "How does the RAG system work?"
- "What color scheme is used?"

**Practical Questions:**
- "How do I create a note?"
- "How do I format markdown?"
- "What emojis are supported?"

## Customizing Example Notes

To modify the example notes, edit the `populateExampleNotes()` method in `Database.java`:

```java
createExampleNote(
    "# Your Title Here",
    "# Your Title Here\n\n" +
    "Your markdown content here...\n\n" +
    "## Section 1\n" +
    "More content..."
);
```

## Resetting to Examples

To reload the example notes:

1. **Delete the database:**
   ```powershell
   Remove-Item notia_db.mv.db
   ```

2. **Clear ChromaDB:**
   ```powershell
   Remove-Item -Recurse chroma_data
   ```

3. **Restart the application:**
   ```powershell
   mvn javafx:run
   ```

The example notes will be recreated on startup!

## Note Structure

Each example note demonstrates:
- ✅ Proper markdown formatting
- ✅ Emoji usage
- ✅ Code blocks
- ✅ Lists and structure
- ✅ Headers and organization
- ✅ Practical, searchable content

## Benefits

The example notes:
- **Teach** users how to use Notia
- **Demonstrate** markdown capabilities
- **Provide** test data for AI chat
- **Showcase** the Rosé Pine theme
- **Document** the application features

## Additional Notes

- Notes are only created if the database is **completely empty**
- If you already have notes, nothing is added
- All examples use real markdown that renders beautifully
- Perfect for testing the AI chat immediately
- Great reference for markdown syntax

Start exploring Notia with these helpful guides right away! 📚✨
