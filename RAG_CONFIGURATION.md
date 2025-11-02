# RAG Configuration Guide

## Overview
You can customize the behavior of the RAG (Retrieval-Augmented Generation) system by modifying settings in `ChatAssistantFactory.java` and `Chat.java`.

## Configuration Parameters

### 1. Temperature (Creativity Control)

**Location**: `ChatAssistantFactory.java`, line 23

```java
private static final double TEMPERATURE = 0.7;
```

**What it does**: Controls the randomness/creativity of AI responses

**Range**: `0.0` to `1.0`

**Recommended values**:
- `0.0` - **Most deterministic** - Same question always gets very similar answers
- `0.3` - **Factual** - Good for precise information retrieval
- `0.5` - **Balanced** - Mix of accuracy and natural language
- `0.7` - **Default** - Good balance for conversational AI
- `0.9` - **Creative** - More varied and creative responses
- `1.0` - **Most random** - Very creative but potentially less accurate

**Example**:
```java
// For very consistent, factual responses
private static final double TEMPERATURE = 0.3;

// For creative summarization and connections
private static final double TEMPERATURE = 0.9;
```

---

### 2. Max Results (Retrieval Count)

**Location**: `ChatAssistantFactory.java`, line 24

```java
private static final int MAX_RESULTS = 3;
```

**What it does**: Number of relevant note segments to retrieve for each query

**Range**: `1` to `10` (practical range)

**Recommended values**:
- `1-2` - Quick answers, less context
- `3-5` - **Default** - Good balance
- `6-10` - More comprehensive answers, but may include less relevant info

**Example**:
```java
// For focused, quick answers
private static final int MAX_RESULTS = 2;

// For comprehensive answers with more context
private static final int MAX_RESULTS = 5;
```

---

### 3. Minimum Score (Similarity Threshold)

**Location**: `ChatAssistantFactory.java`, line 25

```java
private static final double MIN_SCORE = 0.5;
```

**What it does**: Minimum similarity score for a note to be considered relevant

**Range**: `0.0` to `1.0`

**Recommended values**:
- `0.3-0.4` - More results, potentially less relevant
- `0.5` - **Default** - Balanced relevance
- `0.6-0.7` - Stricter matching, fewer but more relevant results
- `0.8+` - Very strict, only highly relevant matches

**Example**:
```java
// For broader search (more results)
private static final double MIN_SCORE = 0.4;

// For strict matching (fewer, more relevant results)
private static final double MIN_SCORE = 0.7;
```

---

### 4. Chat Memory Size

**Location**: `ChatAssistantFactory.java`, line 26

```java
private static final int CHAT_MEMORY_SIZE = 10;
```

**What it does**: Number of previous messages the AI remembers

**Range**: `1` to `50` (practical range)

**Recommended values**:
- `5` - Short-term memory, faster
- `10` - **Default** - Good for conversation context
- `20-30` - Long conversations with context
- `50+` - Extended context (may slow down responses)

**Example**:
```java
// For quick Q&A without much context
private static final int CHAT_MEMORY_SIZE = 5;

// For long, detailed conversations
private static final int CHAT_MEMORY_SIZE = 20;
```

---

### 5. System Prompt (AI Behavior)

**Location**: `ChatAssistantFactory.java`, lines 28-38

```java
private static final String SYSTEM_PROMPT = 
    "You are an intelligent note-taking assistant...";
```

**What it does**: Defines the AI's role, personality, and behavior guidelines

**How to customize**:

**Example 1 - Academic Assistant**:
```java
private static final String SYSTEM_PROMPT = 
    "You are an academic research assistant helping with study notes. " +
    "Provide detailed, analytical responses and help connect concepts. " +
    "Always cite which notes your information comes from. " +
    "Help with summarization, concept explanation, and study guides.";
```

**Example 2 - Technical Documentation Helper**:
```java
private static final String SYSTEM_PROMPT = 
    "You are a technical documentation assistant. " +
    "Help find code snippets, API references, and technical details. " +
    "Provide code examples when relevant. " +
    "Be precise and technical in your responses.";
```

**Example 3 - Personal Journal Assistant**:
```java
private static final String SYSTEM_PROMPT = 
    "You are a personal journal assistant. " +
    "Help users reflect on their thoughts and feelings. " +
    "Be empathetic and supportive. " +
    "Help identify patterns and insights from journal entries. " +
    "Respect privacy and maintain a warm, conversational tone.";
```

**Example 4 - Creative Writing Helper**:
```java
private static final String SYSTEM_PROMPT = 
    "You are a creative writing assistant. " +
    "Help with story ideas, character development, and plot suggestions. " +
    "Reference previous notes to maintain consistency. " +
    "Be encouraging and creative in your responses.";
```

---

## Complete Configuration Examples

### Configuration 1: Factual Knowledge Base
**Use case**: Technical documentation, study notes, reference material

```java
private static final double TEMPERATURE = 0.3;
private static final int MAX_RESULTS = 5;
private static final double MIN_SCORE = 0.6;
private static final int CHAT_MEMORY_SIZE = 10;
private static final String SYSTEM_PROMPT = 
    "You are a precise knowledge assistant. Provide accurate, factual answers " +
    "based strictly on the user's notes. Cite specific notes when possible.";
```

### Configuration 2: Creative Brainstorming
**Use case**: Idea generation, creative writing, connecting concepts

```java
private static final double TEMPERATURE = 0.9;
private static final int MAX_RESULTS = 4;
private static final double MIN_SCORE = 0.4;
private static final int CHAT_MEMORY_SIZE = 15;
private static final String SYSTEM_PROMPT = 
    "You are a creative thinking partner. Help brainstorm ideas, make connections " +
    "between notes, and suggest creative possibilities. Be imaginative and inspiring.";
```

### Configuration 3: Quick Reference
**Use case**: Fast lookups, simple Q&A

```java
private static final double TEMPERATURE = 0.5;
private static final int MAX_RESULTS = 2;
private static final double MIN_SCORE = 0.6;
private static final int CHAT_MEMORY_SIZE = 5;
private static final String SYSTEM_PROMPT = 
    "You are a quick reference assistant. Provide brief, direct answers to queries. " +
    "Get straight to the point.";
```

---

## Model Selection

**Location**: `ChatAssistantFactory.java`, line 45

```java
.modelName("gemini-1.5-flash")
```

**Available Gemini models**:
- `gemini-1.5-flash` - **Default** - Fast, efficient, good for most uses
- `gemini-1.5-pro` - More capable, better reasoning, slower
- `gemini-1.0-pro` - Older version, faster but less capable

**Example**:
```java
// For better quality (slower, more expensive)
.modelName("gemini-1.5-pro")

// For speed (faster, cheaper)
.modelName("gemini-1.5-flash")
```

---

## How to Apply Changes

1. **Edit the file**: `src/main/java/com/notia/ChatAssistantFactory.java`
2. **Modify the constants** at the top of the class
3. **Recompile**: `mvn clean compile`
4. **Run the app**: `mvn javafx:run`

---

## Testing Your Configuration

After changing settings, test with these questions:

1. **Factual retrieval**: "What notes do I have about [topic]?"
2. **Summarization**: "Summarize my notes on [subject]"
3. **Creative**: "What connections can you find between my notes?"
4. **Specific**: "Find the note where I wrote about [keyword]"

Adjust parameters based on response quality!

---

## Advanced: Environment-Based Configuration

You can make settings configurable via environment variables:

```java
private static final double TEMPERATURE = 
    Double.parseDouble(System.getenv().getOrDefault("RAG_TEMPERATURE", "0.7"));
    
private static final int MAX_RESULTS = 
    Integer.parseInt(System.getenv().getOrDefault("RAG_MAX_RESULTS", "3"));
```

Then set before running:
```powershell
$env:RAG_TEMPERATURE="0.9"
$env:RAG_MAX_RESULTS="5"
mvn javafx:run
```

---

## Troubleshooting

**AI responses too random/inconsistent**
- Lower TEMPERATURE (try 0.3-0.5)

**AI not finding relevant notes**
- Lower MIN_SCORE (try 0.4)
- Increase MAX_RESULTS (try 5-7)

**Responses too generic/not from notes**
- Increase MIN_SCORE (try 0.6-0.7)
- Make system prompt more specific
- Ensure notes are saved in database

**AI doesn't remember conversation context**
- Increase CHAT_MEMORY_SIZE (try 15-20)

**Slow responses**
- Reduce MAX_RESULTS
- Switch to "gemini-1.5-flash"
- Reduce CHAT_MEMORY_SIZE