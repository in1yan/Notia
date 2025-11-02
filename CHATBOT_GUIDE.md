# AI Chatbot Sidebar - User Guide

## Overview
Your Notia app now includes an AI-powered chatbot sidebar that can answer questions about your notes using Google Gemini and RAG technology.

## How to Use

### Opening the Chat Sidebar
1. Click the **"AI Chat"** button in the toolbar
2. The chat sidebar will appear on the right side of the screen
3. Click again to hide the sidebar

### Chatting with the AI
1. Type your question in the input field at the bottom
2. Press **Enter** or click **Send**
3. Watch the loading animation while the AI processes your query
4. The AI will respond with information from your notes

### Features

**Smart Animations**
- Messages fade in smoothly when added
- Loading spinner shows when AI is thinking
- Auto-scroll to newest messages

**Message Types**
- **Blue background**: Your messages
- **Gray background**: AI responses
- **Yellow background**: System messages/errors

**Controls**
- **Send button**: Submit your message
- **Clear Chat**: Reset the conversation
- **Input field**: Type your questions (press Enter to send)

### Example Questions
- "What notes do I have about [topic]?"
- "Summarize my notes on [subject]"
- "Find information about [keyword]"
- "What did I write about [concept]?"

## Prerequisites

**Before using the chat:**

1. **Start ChromaDB**:
   ```powershell
   chroma run --host :: --port 8000
   ```

2. **Set Gemini API Key**:
   ```powershell
   $env:GEMINI_API_KEY="your-gemini-api-key"
   ```

3. **Run the App**:
   ```powershell
   mvn javafx:run
   ```

## Technical Details

**UI Components**:
- VBox container with custom styling
- ScrollPane for message history
- TextField for input
- ProgressIndicator for loading state
- Fade animations on message appearance

**Backend**:
- ChatAssistantFactory initializes the AI
- Async message processing (non-blocking UI)
- Platform.runLater() for thread-safe UI updates
- Message history with 10-message memory

**Styling**:
- Custom CSS file: `src/main/resources/chat-styles.css`
- Material Design-inspired color scheme
- Rounded corners and shadows
- Responsive layout

## Troubleshooting

**Chat button does nothing**
- Check that ChromaDB is running
- Verify GEMINI_API_KEY is set
- Look for error messages in console

**"Error: Could not connect to AI service"**
- Ensure ChromaDB is running on port 8000
- Check your Gemini API key is valid
- Verify you have internet connection

**AI gives irrelevant answers**
- Make sure you have notes saved in the database
- Notes need content (not just titles)
- Try more specific questions
- The AI searches for the 2 most relevant notes

**Loading indicator stuck**
- The AI might be processing
- Check your internet connection
- Restart the app if it persists

## Layout

The app now supports three panels:
1. **Left**: Note list (toggleable with "Toggle Sidebar")
2. **Center**: Note editor/preview
3. **Right (Categories/Tags)**: Toggleable with "Toggle Sidebar"
4. **Right (AI Chat)**: Toggleable with "AI Chat" button

Both sidebars can be open at the same time, and the layout adjusts automatically!

## Keyboard Shortcuts

- **Enter** in chat input: Send message
- **Enter** in search field: Search notes

## Customization

You can modify the chat appearance by editing:
- `src/main/resources/chat-styles.css` - Colors, fonts, spacing
- `App.java` - Sidebar width, animation duration
- `ChatAssistantFactory.java` - AI model, retrieval settings
