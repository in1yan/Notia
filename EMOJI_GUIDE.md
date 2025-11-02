# Emoji Support in Notia

## Overview
Notia now fully supports emojis in both the editor and preview views! 🎉

## Features

### ✅ Supported Areas
- **Markdown Editor** - Type emojis directly in your notes
- **HTML Preview** - Emojis render beautifully in preview mode
- **Chat Interface** - Use emojis in chat messages
- **Note Titles** - Add emojis to note titles
- **Chat Responses** - AI can include emojis in responses

## How to Use Emojis

### Windows
- Press `Win + .` (Windows key + period) to open the emoji picker
- Or press `Win + ;` (Windows key + semicolon)

### Mac
- Press `Cmd + Ctrl + Space` to open the emoji picker

### Manual Entry
You can also paste emojis directly from websites like:
- https://emojipedia.org/
- https://getemoji.com/

## Examples

### In Notes
```markdown
# My Shopping List 🛒

- 🍎 Apples
- 🥛 Milk
- 🍞 Bread
- 🧀 Cheese

## Meeting Notes 📝

Today's meeting was productive! ✅
- Discussed project timeline ⏰
- Assigned tasks 📋
- Set deadlines 📅

**Next Steps:** 🚀
1. Complete design mockups 🎨
2. Review code 💻
3. Deploy to production 🌐
```

### In Chat
Ask the AI:
```
"Can you summarize my notes about cooking? 🍳"
"What did I write about my vacation? 🏖️"
"Find my shopping list 🛍️"
```

The AI will respond with emojis too!

## Technical Details

### Fonts Used
The application uses the following font stack for emoji support:
1. **Segoe UI Emoji** - Windows native emoji font
2. **Apple Color Emoji** - macOS emoji font
3. **Noto Color Emoji** - Cross-platform emoji font
4. **sans-serif** - System fallback

### Components with Emoji Support
- `TextArea` (Markdown Editor)
- `WebView` (HTML Preview)
- `TextField` (Chat input)
- `Label` (Chat messages)
- All UI elements via CSS

### HTML Preview
The preview view includes:
- UTF-8 character encoding
- Emoji-compatible font families
- Proper rendering of Unicode characters

## Emoji Categories

### Commonly Used
😀 😃 😄 😁 😆 😅 🤣 😂 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 😚 😙

### Nature & Animals
🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🐤 🦆

### Food & Drink
🍏 🍎 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🫐 🍈 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🍆 🥑

### Activities
⚽ 🏀 🏈 ⚾ 🥎 🎾 🏐 🏉 🥏 🎱 🏓 🏸 🏒 🏑 🥍 🏏 🪃 🥅 ⛳ 🪁

### Travel & Places
🚗 🚕 🚙 🚌 🚎 🏎️ 🚓 🚑 🚒 🚐 🛻 🚚 🚛 🚜 🦯 🦽 🦼 🛴 🚲 🛵

### Objects
📱 💻 ⌨️ 🖥️ 🖨️ 🖱️ 🖲️ 🕹️ 🗜️ 💾 💿 📀 📼 📷 📸 📹 🎥 📞 ☎️

### Symbols
❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❣️ 💕 💞 💓 💗 💖 💘 💝 ✨ 💫

## Tips

1. **Don't Overuse** - While emojis are fun, use them purposefully
2. **Consistency** - Use similar emojis for similar types of notes
3. **Accessibility** - Remember not everyone sees emojis the same way
4. **Search** - Some emojis might not be searchable by text

## Troubleshooting

**Emojis showing as squares (□)**
- Your system might be missing emoji fonts
- On Windows: Update to Windows 10 or later
- On Mac: Update to macOS 10.7 or later

**Emojis showing as black & white**
- This is normal on some older systems
- Color emoji support requires modern fonts

**Copy-paste issues**
- Make sure you're using UTF-8 encoding
- Try the system emoji picker instead

## Example Note Template with Emojis

```markdown
# 📚 Study Notes - [Subject]

## 📅 Date: [Today's Date]

### 🎯 Learning Objectives
- [ ] Objective 1
- [ ] Objective 2
- [ ] Objective 3

### 📝 Key Points
💡 **Important:** [Key concept]

⚠️ **Warning:** [Common mistake]

✅ **Remember:** [Crucial detail]

### 🔗 Resources
- 📖 Textbook: Chapter X
- 🌐 Website: [URL]
- 🎥 Video: [Link]

### 📊 Summary
[Your summary here]

### ❓ Questions
1. Question 1?
2. Question 2?

---
*Created with ❤️ using Notia*
```

Enjoy using emojis in your notes! 🎉✨
