# Architecture Overview

Apollo Pocket Assistant follows **MVVM with Clean Architecture**, divided into three layers:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Screens    │  │  ViewModels │  │  UI Components       │ │
│  │  (Compose)  │  │  (StateFlow)│  │  (Reusable)          │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Use Cases  │  │  Entities   │  │  Repository          │ │
│  │             │  │             │  │  Interfaces          │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Room DB    │  │  DataStore  │  │  LLM Client         │ │
│  │  (Memory)   │  │  (Prefs)    │  │  (Ollama)           │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  MCP Adapter│  │  Device     │  │  Script             │ │
│  │             │  │  Bridge     │  │  Executor           │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
app/src/main/java/com/appolopocket/
├── di/                    # Hilt dependency injection modules
├── data/
│   ├── local/             # Room database, DAOs, DataStore
│   ├── remote/            # OllamaClient, Retrofit services
│   ├── repository/        # Repository implementations
│   └── mcp/               # Model Context Protocol handler
├── domain/
│   ├── model/             # Domain entities
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic use cases
├── ui/
│   ├── theme/             # Vaporwave Material 3 theme + typography
│   ├── components/        # Reusable composables (ASCII art, chat bubbles)
│   ├── screens/           # Screen composables (Main, Settings)
│   └── viewmodel/         # ChatViewModel, SettingsViewModel
├── service/               # Android background services
│   ├── LLMService         # Foreground service for LLM inference
│   ├── AppoloAccessibilityService
│   └── NotificationListenerService
└── util/                  # DeviceController, ScriptExecutor
```

## Key Technology Choices

| Concern | Choice | Rationale |
|---------|--------|-----------|
| UI | Jetpack Compose + Material 3 | Declarative, modern Android UI |
| DI | Hilt 2.52 | Official Android DI, minimal boilerplate |
| Async | Kotlin Coroutines + Flow | Reactive, lifecycle-aware data streams |
| Local DB | Room 2.6 | Type-safe SQLite with coroutine support |
| Preferences | DataStore 1.1 | Replaces SharedPreferences, async-safe |
| Networking | Retrofit 2.11 + OkHttp 4.12 | Industry standard HTTP client |
| Local LLM | Ollama HTTP API | Run models like Llama 3 / Mistral locally |
| Serialization | Kotlinx Serialization 1.7 | Native Kotlin JSON, compile-time safe |

## State Management

- **StateFlow** is the single source of truth in every ViewModel.
- **Channels** are used for one-time side effects (navigation events, toasts).
- **Repository pattern** abstracts data sources; the domain layer never touches Room or Retrofit directly.

## LLM Integration

`OllamaClient` communicates with a locally running Ollama server (default `http://localhost:11434`) over Retrofit. Streaming responses are exposed as a `Flow<String>` that the `ChatViewModel` collects and appends to the UI state.

## MCP (Model Context Protocol)

`MCPHandler` implements a lightweight JSON-RPC 2.0 handler that both:
- **Client mode**: discovers and invokes tools hosted on external MCP servers.
- **Server mode**: exposes Apollo's own device-control capabilities as MCP tools.
