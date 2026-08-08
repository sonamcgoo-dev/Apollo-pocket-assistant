# Appolo Pocket - AI Super Assistant Specification

## 1. Project Overview

**Project Name:** Appolo Pocket  
**Type:** Android Native AI Assistant Application  
**Core Functionality:** A fully autonomous, local-first AI super assistant that provides device control, automation, code execution, and intelligent task management through a vaporwave-themed CLI-style interface with integrated local LLM capabilities.

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Kotlin 1.9.x
- **UI Framework:** Jetpack Compose (Material 3)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Key Libraries & Dependencies
| Category | Library | Purpose |
|----------|---------|---------|
| UI | Jetpack Compose BOM 2024.02.00 | Modern declarative UI |
| DI | Hilt 2.50 | Dependency injection |
| Navigation | Compose Navigation 2.7.7 | Screen navigation |
| Async | Kotlin Coroutines + Flow | Reactive programming |
| Local LLM | Ollama Android SDK | Local model inference |
| Database | Room 2.6.1 | Local data persistence |
| Preferences | DataStore 1.0.0 | User preferences |
| Networking | Retrofit 2.9.0 + OkHttp | API communication |
| JSON | Kotlinx Serialization | JSON parsing |
| Markdown | Commonmark | Prompt file parsing |
| Shell | Chilkat / custom Process | Code/script execution |
| Accessibility | Uiautomator2 | App automation |
| Root | libsu | Root privilege access |

### State Management
- **Architecture:** MVVM with Clean Architecture
- **State Holder:** ViewModel + StateFlow
- **Side Effects:** Kotlin Channels for one-time events
- **Dependency Flow:** Single source of truth via Repository pattern

### Architecture Pattern
```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐│
│  │  Screens    │  │  ViewModels │  │  UI Components       ││
│  │  (Compose)  │  │  (State)    │  │  (Reusable)          ││
│  └─────────────┘  └─────────────┘  └─────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐│
│  │  Use Cases  │  │  Entities   │  │  Repository          ││
│  │             │  │             │  │  Interfaces         ││
│  └─────────────┘  └─────────────┘  └─────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐│
│  │  Room DB    │  │  DataStore  │  │  LLM Client         ││
│  │  (Memory)   │  │  (Prefs)    │  │  (Ollama)          ││
│  └─────────────┘  └─────────────┘  └─────────────────────┘│
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐│
│  │  MCP Server │  │  Device     │  │  Script             ││
│  │  Adapter    │  │  Bridge     │  │  Executor           ││
│  └─────────────┘  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

## 3. Feature List

### Core AI Capabilities
- [ ] Local LLM integration via Ollama (supports Llama2, Mistral, Phi, etc.)
- [ ] 4 configurable control prompts: System, Agent, Personality, Functions/Scope
- [ ] Prompt files in Markdown/JSON format (editable by user or agent)
- [ ] Persistent conversation memory with context window management
- [ ] Task planning and execution tracking
- [ ] 99% auto-approval for routine tasks (configurable)

### Device Control
- [ ] Filesystem read/write/explore (internal, external, root paths)
- [ ] System settings read/write (WiFi, Bluetooth, Brightness, etc.)
- [ ] App installation, launch, and interaction via Accessibility
- [ ] Root privilege execution (su commands)
- [ ] Kernel parameter access (sys/class, proc)

### Communication
- [ ] SMS read, compose, and send
- [ ] Email compose and send (via Intent)
- [ ] Notification listener and filtering
- [ ] Quick replies from notifications

### Automation & Scheduling
- [ ] MCP (Model Context Protocol) client/server
- [ ] MCP server hosting for external tools
- [ ] Cron-like task scheduling
- [ ] Accessibility service for app automation
- [ ] Macro recording and playback

### Code & Scripting
- [ ] Python script execution
- [ ] Shell/Bash script execution
- [ ] JavaScript/Node.js execution
- [ ] Code output capture and display
- [ ] Multi-file project support

### Web Capabilities
- [ ] Web search integration
- [ ] HTML scraping (small scale)
- [ ] API endpoint testing
- [ ] Content extraction and summarization

### Memory System
- [ ] Conversation history persistence
- [ ] Long-term fact storage
- [ ] Task completion memory
- [ ] User preference learning

## 4. UI/UX Design Direction

### Overall Visual Style
**Vaporwave Cyber-Antique** - A fusion of 80s retro-futurism, Greek mythology aesthetics, and modern neon cyberpunk. The god Apollo as the central deity represents knowledge, music, and prophecy.

### Color Scheme
| Element | Color | Hex |
|---------|-------|-----|
| Primary | Neon Magenta | #FF00FF |
| Secondary | Electric Cyan | #00FFFF |
| Tertiary | Vaporwave Purple | #8B5CF6 |
| Background Dark | Deep Night | #0D0221 |
| Background Light | Soft Lavender | #1A0A2E |
| Surface | Glassy Purple | #2D1B4E |
| Text Primary | Soft White | #F0E6FF |
| Text Secondary | Muted Lavender | #A78BFA |
| Success | Neon Green | #39FF14 |
| Error | Hot Pink | #FF1493 |
| Accent | Golden Bronze | #CD7F32 |

### Typography
- **Primary Font:** JetBrains Mono (monospace, code/CLI feel)
- **Display Font:** Custom or retrofuturistic font for headers
- **Fallback:** System default sans-serif

### Layout Approach
- **Main Interface:** Full-screen CLI-style terminal with message history
- **Navigation:** Bottom sheet for quick actions, swipe gestures
- **Quick Tiles:** Expandable floating action panel (3-5 quick actions)
- **Settings:** Side drawer with tabbed sections
- **Popups:** Modal overlays with glassmorphism effect

### Key UI Components
1. **Terminal View** - Scrolling message list with typewriter effect
2. **Command Input** - Bottom-positioned text field with auto-complete
3. **Quick Tiles** - Floating panel: Voice, Camera, Files, Search, Settings
4. **ASCII Art Animations** - Segway transitions, loading states, backgrounds
5. **Glassmorphic Cards** - Semi-transparent overlays for modals
6. **Neon Glow Effects** - Subtle animated borders and shadows
7. **Apollo Bust Icon** - Vaporwave-styled god Apollo as app icon

### ASCII Art Themes
- App startup splash animation
- Mode transitions (code mode, chat mode, automation mode)
- Loading indicators
- Background decorative elements
- Success/failure celebrations

## 5. Control Prompts Structure

### System Prompt (system.md)
```
# Appolo Pocket System Configuration
- Core identity and purpose
- Operating boundaries
- Safety guidelines
- Local-first philosophy
```

### Agent Prompt (agent.md)
```
# Agent Behavior Instructions
- Task execution methodology
- Memory management
- Planning approach
- Error handling
```

### Personality Prompt (personality.md)
```
# Personality Configuration
- Tone and communication style
- Vaporwave aesthetic references
- Humor and creativity
- Emotional intelligence
```

### Functions & Scope (functions.json)
```json
{
  "capabilities": [...],
  "permissions": [...],
  "auto_approve_rules": [...],
  "mcp_tools": [...]
}
```

## 6. Project Structure

```
appolo-pocket/
├── app/
│   └── src/main/
│       ├── java/com/appolopocket/
│       │   ├── di/                    # Hilt modules
│       │   ├── data/
│       │   │   ├── local/             # Room, DataStore
│       │   │   ├── remote/            # LLM client, APIs
│       │   │   ├── repository/        # Repository implementations
│       │   │   └── mcp/               # MCP protocol
│       │   ├── domain/
│       │   │   ├── model/             # Domain entities
│       │   │   ├── repository/        # Repository interfaces
│       │   │   └── usecase/           # Business logic
│       │   ├── ui/
│       │   │   ├── theme/             # Vaporwave theme
│       │   │   ├── components/        # Reusable UI
│       │   │   ├── screens/           # Screen composables
│       │   │   └── viewmodel/         # ViewModels
│       │   ├── service/              # Android services
│       │   └── util/                  # Utilities
│       ├── res/
│       │   ├── drawable/              # Icons, graphics
│       │   └── raw/                   # Static prompts
│       └── assets/
│           └── prompts/              # Editable prompt files
├── build.gradle.kts
└── settings.gradle.kts
```

## 7. MCP Integration

### MCP Client Mode
- Connect to external MCP servers for extended capabilities
- Tool registration and invocation
- Server discovery via stdio or HTTP

### MCP Server Mode
- Expose Appolo's capabilities as MCP tools
- Protocol handler for JSON-RPC 2.0
- Tool schema definitions

## 8. Auto-Approval System

Configurable approval bypass levels:
- **Full Auto:** Approve all non-destructive tasks
- **Smart Auto:** Approve based on learned patterns
- **Manual:** Require confirmation for all
- **Custom Rules:** JSON-based rule engine

Default rules (99% coverage):
- File reads
- Code execution (sandboxed)
- App launches
- Search queries
- Notification reads
- Message composition (draft only)
- Calendar reads

Protected actions (always require approval):
- File deletions
- System settings changes
- App installations
- Root commands
- Data exports

---

*Document Version: 1.0*  
*Created for: Appolo Pocket AI Assistant*
