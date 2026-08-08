package com.appolopocket.data.mcp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Model Context Protocol (MCP) Handler
 * Implements JSON-RPC 2.0 based MCP protocol for tool invocation
 */
class MCPHandler {
    
    sealed class MCPMessage {
        @Serializable
        data class Request(
            val jsonrpc: String = "2.0",
            val id: String = UUID.randomUUID().toString(),
            val method: String,
            val params: JsonObject? = null
        ) : MCPMessage()
        
        @Serializable
        data class Response(
            val jsonrpc: String = "2.0",
            val id: String,
            val result: JsonElement? = null,
            val error: ErrorObject? = null
        ) : MCPMessage()
        
        @Serializable
        data class Notification(
            val jsonrpc: String = "2.0",
            val method: String,
            val params: JsonObject? = null
        ) : MCPMessage()
        
        @Serializable
        data class ErrorObject(
            val code: Int,
            val message: String,
            val data: JsonElement? = null
        )
    }
    
    // Event types
    sealed class MCPToolEvent {
        data class ToolCall(val name: String, val arguments: Map<String, Any>) : MCPToolEvent()
        data class ToolResult(val callId: String, val result: Any?, val error: String? = null) : MCPToolEvent()
        data class ToolRegistered(val tool: MCPTool) : MCPToolEvent()
    }
    
    @Serializable
    data class MCPTool(
        val name: String,
        val description: String,
        val inputSchema: Map<String, JsonElement>
    )
    
    @Serializable
    data class MCPToolResult(
        val success: Boolean,
        val output: String? = null,
        val error: String? = null,
        val metadata: Map<String, String>? = null
    )
    
    // Protocol methods
    object Methods {
        const val INITIALIZE = "initialize"
        const val TOOLS_LIST = "tools/list"
        const val TOOLS_CALL = "tools/call"
        const val PROMPTS_LIST = "prompts/list"
        const val RESOURCES_LIST = "resources/list"
        const val RESOURCES_READ = "resources/read"
        const val PING = "ping"
        const val CANCEL = "cancel"
    }
    
    // Error codes
    object ErrorCodes {
        const val PARSE_ERROR = -32700
        const val INVALID_REQUEST = -32600
        const val METHOD_NOT_FOUND = -32601
        const val INVALID_PARAMS = -32602
        const val INTERNAL_ERROR = -32603
        const val TOOL_ERROR = -32000
    }
    
    private val _events = MutableSharedFlow<MCPToolEvent>()
    val events = _events.asSharedFlow()
    
    private val registeredTools = mutableMapOf<String, MCPTool>()
    private val toolHandlers = mutableMapOf<String, suspend (Map<String, Any>) -> MCPToolResult>()
    
    fun registerTool(tool: MCPTool, handler: suspend (Map<String, Any>) -> MCPToolResult) {
        registeredTools[tool.name] = tool
        toolHandlers[tool.name] = handler
    }
    
    fun unregisterTool(name: String) {
        registeredTools.remove(name)
        toolHandlers.remove(name)
    }
    
    fun getRegisteredTools(): List<MCPTool> = registeredTools.values.toList()
    
    suspend fun handleMessage(message: String): String {
        return try {
            val json = Json.parseToJsonElement(message).jsonObject
            
            when {
                json.containsKey("method") -> handleRequest(json)
                json.containsKey("result") || json.containsKey("error") -> handleResponse(json)
                else -> createErrorResponse("", MCPMessage.ErrorObject(
                    ErrorCodes.INVALID_REQUEST,
                    "Invalid message format"
                ))
            }
        } catch (e: Exception) {
            createErrorResponse("", MCPMessage.ErrorObject(
                ErrorCodes.PARSE_ERROR,
                "Parse error: ${e.message}"
            ))
        }
    }
    
    private suspend fun handleRequest(json: JsonObject): String {
        val id = json["id"]?.jsonPrimitive?.content ?: ""
        val method = json["method"]?.jsonPrimitive?.content ?: ""
        val params = json["params"]?.jsonObject
        
        return when (method) {
            Methods.INITIALIZE -> handleInitialize(id, params)
            Methods.TOOLS_LIST -> handleToolsList(id)
            Methods.TOOLS_CALL -> handleToolsCall(id, params)
            Methods.PING -> handlePing(id)
            else -> createErrorResponse(id, MCPMessage.ErrorObject(
                ErrorCodes.METHOD_NOT_FOUND,
                "Method not found: $method"
            ))
        }
    }
    
    private fun handleRequest(json: JsonObject, callback: (String) -> Unit) {
        // For async handling via callback
    }
    
    private fun handleResponse(json: JsonObject): String {
        // Handle responses (for client mode)
        return ""
    }
    
    private fun handleInitialize(id: String, params: JsonObject?): String {
        val result = buildJsonObject {
            put("protocolVersion", "1.0")
            put("capabilities", buildJsonObject {
                put("tools", true)
                put("prompts", true)
                put("resources", true)
            })
            put("serverInfo", buildJsonObject {
                put("name", "appolo-pocket")
                put("version", "1.0.0")
            })
        }
        
        return createSuccessResponse(id, result)
    }
    
    private fun handleToolsList(id: String): String {
        val tools = registeredTools.values.map { tool ->
            buildJsonObject {
                put("name", tool.name)
                put("description", tool.description)
                put("inputSchema", Json.encodeToJsonElement(tool.inputSchema))
            }
        }
        
        val result = buildJsonObject {
            put("tools", JsonArray(tools))
        }
        
        return createSuccessResponse(id, result)
    }
    
    private suspend fun handleToolsCall(id: String, params: JsonObject?): String {
        if (params == null) {
            return createErrorResponse(id, MCPMessage.ErrorObject(
                ErrorCodes.INVALID_PARAMS,
                "Missing parameters"
            ))
        }
        
        val toolName = params["name"]?.jsonPrimitive?.content
        val arguments = params["arguments"]?.jsonObject?.mapValues { it.value.toString() } ?: emptyMap()
        
        if (toolName == null || !toolHandlers.containsKey(toolName)) {
            return createErrorResponse(id, MCPMessage.ErrorObject(
                ErrorCodes.METHOD_NOT_FOUND,
                "Tool not found: $toolName"
            ))
        }
        
        return try {
            val handler = toolHandlers[toolName]!!
            val result = handler(arguments)
            
            val toolResult = buildJsonObject {
                put("success", result.success)
                result.output?.let { put("output", it) }
                result.error?.let { put("error", it) }
            }
            
            createSuccessResponse(id, toolResult)
        } catch (e: Exception) {
            createErrorResponse(id, MCPMessage.ErrorObject(
                ErrorCodes.TOOL_ERROR,
                "Tool execution failed: ${e.message}"
            ))
        }
    }
    
    private fun handlePing(id: String): String {
        val result = buildJsonObject {
            put("pong", true)
        }
        return createSuccessResponse(id, result)
    }
    
    private fun createSuccessResponse(id: String, result: JsonElement): String {
        val response = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", result)
        }
        return Json.encodeToString(JsonObject.serializer(), response)
    }
    
    private fun createErrorResponse(id: String, error: MCPMessage.ErrorObject): String {
        val response = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", id)
            put("error", buildJsonObject {
                put("code", error.code)
                put("message", error.message)
                error.data?.let { put("data", it) }
            })
        }
        return Json.encodeToString(JsonObject.serializer(), response)
    }
    
    // Client mode methods
    suspend fun callTool(toolName: String, arguments: Map<String, Any>): Result<MCPToolResult> {
        if (!toolHandlers.containsKey(toolName)) {
            return Result.failure(Exception("Tool not found: $toolName"))
        }
        
        return try {
            val handler = toolHandlers[toolName]!!
            val result = handler(arguments)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun createToolCallRequest(toolName: String, arguments: Map<String, Any>): String {
        val request = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", UUID.randomUUID().toString())
            put("method", Methods.TOOLS_CALL)
            put("params", buildJsonObject {
                put("name", toolName)
                put("arguments", Json.encodeToJsonElement(arguments))
            })
        }
        return Json.encodeToString(JsonObject.serializer(), request)
    }
}

/**
 * MCP Server that hosts Appolo's capabilities
 */
class MCPServer(private val handler: MCPHandler) {
    
    private var isRunning = false
    
    fun registerDefaultTools(
        fileTools: FileTools,
        deviceTools: DeviceTools,
        memoryTools: MemoryTools
    ) {
        // Register file tools
        handler.registerTool(
            MCPHandler.MCPTool(
                name = "read_file",
                description = "Read file contents",
                inputSchema = mapOf(
                    "path" to JsonPrimitive("string")
                )
            )
        ) { args ->
            fileTools.readFile(args["path"] as String)
        }
        
        handler.registerTool(
            MCPHandler.MCPTool(
                name = "write_file",
                description = "Write content to file",
                inputSchema = mapOf(
                    "path" to JsonPrimitive("string"),
                    "content" to JsonPrimitive("string")
                )
            )
        ) { args ->
            fileTools.writeFile(args["path"] as String, args["content"] as String)
        }
        
        // Register device tools
        handler.registerTool(
            MCPHandler.MCPTool(
                name = "get_device_info",
                description = "Get device information",
                inputSchema = emptyMap()
            )
        ) { _ ->
            deviceTools.getDeviceInfo()
        }
        
        // Register memory tools
        handler.registerTool(
            MCPHandler.MCPTool(
                name = "store_memory",
                description = "Store information in memory",
                inputSchema = mapOf(
                    "key" to JsonPrimitive("string"),
                    "value" to JsonPrimitive("string")
                )
            )
        ) { args ->
            memoryTools.storeMemory(args["key"] as String, args["value"] as String)
        }
    }
    
    suspend fun processMessage(message: String): String {
        return handler.handleMessage(message)
    }
    
    fun start() {
        isRunning = true
    }
    
    fun stop() {
        isRunning = false
    }
    
    fun isRunning() = isRunning
}

// Tool implementations (to be connected to actual implementations)
class FileTools {
    suspend fun readFile(path: String): MCPHandler.MCPToolResult = MCPHandler.MCPToolResult(
        success = true,
        output = "File reading implementation"
    )
    
    suspend fun writeFile(path: String, content: String): MCPHandler.MCPToolResult = MCPHandler.MCPToolResult(
        success = true,
        output = "File written successfully"
    )
}

class DeviceTools {
    suspend fun getDeviceInfo(): MCPHandler.MCPToolResult = MCPHandler.MCPToolResult(
        success = true,
        output = """{"manufacturer":"Android","model":"Device"}"""
    )
}

class MemoryTools {
    suspend fun storeMemory(key: String, value: String): MCPHandler.MCPToolResult = MCPHandler.MCPToolResult(
        success = true,
        output = "Memory stored"
    )
}
