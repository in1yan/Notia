# Quick Configuration Cheat Sheet

## RAG Settings at a Glance

### File: `ChatAssistantFactory.java`

```java
// LINE 23: How creative/random should responses be?
private static final double TEMPERATURE = 0.7;  // 0.0 = deterministic, 1.0 = creative

// LINE 24: How many notes to retrieve per query?
private static final int MAX_RESULTS = 3;  // 1-10 notes

// LINE 25: How similar must notes be to match?
private static final double MIN_SCORE = 0.5;  // 0.0 = any match, 1.0 = exact match

// LINE 26: How many messages to remember?
private static final int CHAT_MEMORY_SIZE = 10;  // Previous messages

// LINES 28-38: What is the AI's personality/role?
private static final String SYSTEM_PROMPT = "...";
```

---

## Common Presets

### Preset 1: Factual Reference [BOOKS]
```java
TEMPERATURE = 0.3;      // Consistent, factual
MAX_RESULTS = 5;        // More context
MIN_SCORE = 0.6;        // Strict matching
CHAT_MEMORY_SIZE = 10;  // Standard memory
```
**Best for**: Documentation, study notes, technical references

---

### Preset 2: Creative Assistant [LIGHTBULB]
```java
TEMPERATURE = 0.9;      // Very creative
MAX_RESULTS = 4;        // Good context
MIN_SCORE = 0.4;        // Loose matching
CHAT_MEMORY_SIZE = 15;  // Long context
```
**Best for**: Brainstorming, creative writing, idea generation

---

### Preset 3: Quick Lookup [LIGHTNING]
```java
TEMPERATURE = 0.5;      // Balanced
MAX_RESULTS = 2;        // Fast retrieval
MIN_SCORE = 0.6;        // Relevant only
CHAT_MEMORY_SIZE = 5;   // Short memory
```
**Best for**: Fast Q&A, simple lookups

---

## Quick Fixes

| Problem | Solution |
|---------|----------|
| Responses too random | Lower `TEMPERATURE` to `0.3` |
| Can't find notes | Lower `MIN_SCORE` to `0.4` |
| Missing context | Increase `MAX_RESULTS` to `5` |
| Forgot conversation | Increase `CHAT_MEMORY_SIZE` to `20` |
| Too slow | Reduce `MAX_RESULTS` to `2` |
| Too generic | Increase `MIN_SCORE` to `0.7` |

---

## System Prompt Templates

### Academic [GRADUATION CAP]
```java
"You are an academic research assistant helping with study notes. " +
"Provide detailed, analytical responses. Always cite sources."
```

### Technical [COMPUTER]
```java
"You are a technical documentation assistant. " +
"Help find code snippets and API references. Be precise and technical."
```

### Personal Journal [NOTEBOOK]
```java
"You are a personal journal assistant. " +
"Be empathetic and help identify patterns in thoughts and feelings."
```

### Creative Writing [PENCIL]
```java
"You are a creative writing assistant. " +
"Help with story ideas, character development, and plot suggestions."
```

---

## Parameter Impact

### Temperature
```
0.0  ==================  1.0
   Consistent          Creative
   Factual             Varied
   Boring              Exciting
   Safe                Risky
```

### Max Results
```
1    ==================  10
   Fast                Slow
   Focused             Comprehensive
   Simple              Detailed
```

### Min Score
```
0.0  ==================  1.0
   Any match           Exact match
   More results        Fewer results
   Broad               Strict
```

---

## Apply Changes

```bash
# 1. Edit the file
notepad src/main/java/com/notia/ChatAssistantFactory.java

# 2. Recompile
mvn clean compile

# 3. Run
mvn javafx:run
```

---

## Test Your Changes

Ask these questions to test:

1. **"What notes do I have about [topic]?"** - Tests retrieval
2. **"Summarize my notes"** - Tests understanding
3. **"What connections do you see?"** - Tests creativity
4. **"Do you remember what I asked earlier?"** - Tests memory

---

## Notes

- Changes require recompiling the app
- Start with default values and adjust based on results
- Higher temperature = more varied responses
- Lower min_score = more results (potentially less relevant)
- More max_results = slower but more comprehensive

See `RAG_CONFIGURATION.md` for detailed explanations!
