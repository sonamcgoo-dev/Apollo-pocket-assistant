package com.appolopocket.data.remote.llm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.appolopocket.domain.model.LLMConfig
import com.appolopocket.domain.model.Message
import com.appolopocket.domain.model.MessageRole
import com.appolopocket.domain.model.ModelSource
import com.appolopocket.domain.model.ToolCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class OllamaRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = true,
    val options: OllamaOptions? = null,
    val tools: List<OllamaTool>? = null
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String,
    val toolCalls: List<OllamaToolCall>? = null
)

@Serializable
data class OllamaToolCall(
    val function: OllamaFunctionCall
)

@Serializable
data class OllamaFunctionCall(
    val name: String,
    val arguments: Map<String, JsonElement>
)

@Serializable
data class OllamaOptions(
    val temperature: Float? = null,
    val num_predict: Int? = null,
    val top_k: Int? = null,
    val top_p: Float? = null
)

@Serializable
data class OllamaTool(
    val type: String = "function",
    val function: OllamaFunction
)

@Serializable
data class OllamaFunction(
    val name: String,
    val description: String,
    val parameters: OllamaParameters
)

@Serializable
data class OllamaParameters(
    val type: String = "object",
    val required: List<String> = emptyList(),
    val properties: Map<String, OllamaProperty> = emptyMap()
)

@Serializable
data class OllamaProperty(
    val type: String,
    val description: String? = null
)

@Singleton
class OllamaClient @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private var config: LLMConfig = LLMConfig()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    fun updateConfig(newConfig: LLMConfig) {
        config = newConfig
    }
    
    fun getCurrentConfig(): LLMConfig = config
    
    suspend fun chat(
        messages: List<Message>,
        tools: List<OllamaTool> = emptyList()
    ): Flow<LLMResponse> = flow {
        val ollamaMessages = messages.map { it.toOllamaMessage() }
        
        val requestBody = OllamaRequest(
            model = config.modelName,
            messages = ollamaMessages,
            stream = config.stream,
            options = OllamaOptions(
                temperature = config.temperature,
                num_predict = config.maxTokens
            ),
            tools = tools.ifEmpty { null }
        )
        
        val request = Request.Builder()
            .url("${config.baseUrl}/api/chat")
            .post(
                json.encodeToString(requestBody)
                    .toRequestBody("application/json".toMediaType())
            )
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            emit(LLMResponse.Error("HTTP ${response.code}: ${response.message}"))
            return@flow
        }
        
        val body = response.body
        if (body == null) {
            emit(LLMResponse.Error("Empty response body"))
            return@flow
        }
        
        val source = body.source()
        
        while (!source.buffer.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            if (line.isBlank()) continue
            
            try {
                val chunk = json.decodeFromString<OllamaResponseJson>(line)
                
                when {
                    chunk.error != null -> {
                        emit(LLMResponse.Error(chunk.error))
                    }
                    chunk.message != null -> {
                        val content = chunk.message.content ?: ""
                        val toolCalls = chunk.message.toolCalls?.map { tc ->
                            ToolCall(
                                id = java.util.UUID.randomUUID().toString(),
                                name = tc.function.name,
                                arguments = tc.function.arguments.mapValues { entry ->
                                    entry.value.toString().removeSurrounding("\"")
                                }
                            )
                        } ?: emptyList()
                        
                        emit(LLMResponse.Content(
                            content = content,
                            toolCalls = toolCalls,
                            done = chunk.done ?: false,
                            totalDuration = chunk.totalDuration,
                            evalCount = chunk.evalCount,
                            evalDuration = chunk.evalDuration,
                            loadDuration = chunk.loadDuration,
                            promptEvalCount = chunk.promptEvalCount,
                            promptEvalDuration = chunk.promptEvalDuration
                        ))
                    }
                }
            } catch (e: Exception) {
                emit(LLMResponse.Error("Parse error: ${e.message}"))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun generate(
        prompt: String,
        systemPrompt: String? = null
    ): Flow<LLMResponse> = flow {
        val requestBody = buildString {
            append("{")
            append("\"model\": \"${config.modelName}\",")
            append("\"prompt\": ${json.encodeToString(prompt)},")
            append("\"stream\": true,")
            append("\"options\": {")
            append("\"temperature\": ${config.temperature},")
            append("\"num_predict\": ${config.maxTokens}")
            append("}")
            if (systemPrompt != null) {
                append(",\"system\": ${json.encodeToString(systemPrompt)}")
            }
            append("}")
        }
        
        val request = Request.Builder()
            .url("${config.baseUrl}/api/generate")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            emit(LLMResponse.Error("HTTP ${response.code}: ${response.message}"))
            return@flow
        }
        
        val body = response.body
        if (body == null) {
            emit(LLMResponse.Error("Empty response body"))
            return@flow
        }
        
        val source = body.source()
        
        while (!source.buffer.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            if (line.isBlank()) continue
            
            try {
                val chunk = json.decodeFromString<OllamaGenerateResponseJson>(line)
                
                if (chunk.error != null) {
                    emit(LLMResponse.Error(chunk.error))
                } else {
                    emit(LLMResponse.Content(
                        content = chunk.response ?: "",
                        done = chunk.done ?: false,
                        totalDuration = chunk.totalDuration,
                        evalCount = chunk.evalCount,
                        evalDuration = chunk.evalDuration,
                        loadDuration = chunk.loadDuration
                    ))
                }
            } catch (e: Exception) {
                emit(LLMResponse.Error("Parse error: ${e.message}"))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun listModels(): Result<List<OllamaModel>> {
        return when (config.modelSource) {
            ModelSource.OLLAMA -> listOllamaModels()
            ModelSource.HUGGING_FACE -> Result.success(huggingFaceRecommendedModels)
            ModelSource.GITHUB -> Result.success(githubRecommendedModels)
        }
    }
    
    suspend fun pullModel(modelName: String): Flow<ModelPullProgress> = flow {
        when (config.modelSource) {
            ModelSource.OLLAMA -> emitAllOllamaPullProgress(modelName, this)
            ModelSource.HUGGING_FACE -> downloadRemoteModel(modelName, huggingFaceRecommendedModels, this)
            ModelSource.GITHUB -> downloadRemoteModel(modelName, githubRecommendedModels, this)
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun checkConnection(): Boolean {
        return checkOllamaConnection()
    }

    private suspend fun listOllamaModels(): Result<List<OllamaModel>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${config.baseUrl}/api/tags")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response body"))

            val modelsResponse = json.decodeFromString<OllamaModelsResponse>(body)
            Result.success(modelsResponse.models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun emitAllOllamaPullProgress(
        modelName: String,
        collector: FlowCollector<ModelPullProgress>
    ) {
        val requestBody = "{\"name\": \"$modelName\"}"
        val request = Request.Builder()
            .url("${config.baseUrl}/api/pull")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            collector.emit(ModelPullProgress.Error("HTTP ${response.code}: ${response.message}"))
            return
        }

        val body = response.body
        if (body == null) {
            collector.emit(ModelPullProgress.Error("Empty response body"))
            return
        }

        val source = body.source()
        while (!source.buffer.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            if (line.isBlank()) continue

            try {
                val progress = json.decodeFromString<OllamaPullProgress>(line)
                collector.emit(
                    ModelPullProgress.Update(
                        status = progress.status ?: "",
                        digest = progress.digest,
                        total = progress.total,
                        completed = progress.completed
                    )
                )

                if (progress.done == true) {
                    collector.emit(ModelPullProgress.Complete)
                }
            } catch (e: Exception) {
                collector.emit(ModelPullProgress.Error("Parse error: ${e.message}"))
            }
        }
    }

    private suspend fun downloadRemoteModel(
        modelName: String,
        catalog: List<OllamaModel>,
        collector: FlowCollector<ModelPullProgress>
    ) {
        val selectedModel = catalog.firstOrNull { it.name == modelName || it.model == modelName }
        val downloadUrl = selectedModel?.downloadUrl
        if (downloadUrl.isNullOrBlank()) {
            collector.emit(ModelPullProgress.Error("Model download URL not found for $modelName"))
            return
        }

        val modelsDir = File(appContext.filesDir, "models")
        if (!modelsDir.exists() && !modelsDir.mkdirs()) {
            collector.emit(ModelPullProgress.Error("Failed to create model directory"))
            return
        }
        val safeName = modelName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val extension = downloadUrl
            .substringBefore('?')
            .substringAfterLast('.', "")
            .takeIf { it.isNotBlank() } ?: "bin"
        val destination = File(modelsDir, "$safeName.$extension")

        val request = Request.Builder().url(downloadUrl).get().build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            collector.emit(ModelPullProgress.Error("HTTP ${response.code}: ${response.message}"))
            return
        }

        val body = response.body
        if (body == null) {
            collector.emit(ModelPullProgress.Error("Empty response body"))
            return
        }

        val totalBytes = body.contentLength().takeIf { it > 0 } ?: 0L
        collector.emit(
            ModelPullProgress.Update(
                status = "Downloading",
                digest = selectedModel.digest,
                total = totalBytes,
                completed = 0
            )
        )

        body.byteStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(256 * 1024)
                var downloaded = 0L
                var lastEmitted = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    val shouldEmit = (totalBytes > 0 && downloaded == totalBytes) ||
                        downloaded - lastEmitted >= (4 * 1024 * 1024)
                    if (shouldEmit) {
                        collector.emit(
                            ModelPullProgress.Update(
                                status = "Downloading",
                                digest = selectedModel.digest,
                                total = totalBytes,
                                completed = downloaded
                            )
                        )
                        lastEmitted = downloaded
                    }
                }

                if (totalBytes > 0 && downloaded < totalBytes) {
                    destination.delete()
                    collector.emit(
                        ModelPullProgress.Error(
                            "Download incomplete: $downloaded/$totalBytes bytes received"
                        )
                    )
                    return@downloadRemoteModel
                }
            }
        }

        collector.emit(
            ModelPullProgress.Update(
                status = "Saved to ${destination.absolutePath}",
                digest = selectedModel.digest,
                total = totalBytes,
                completed = destination.length()
            )
        )
        collector.emit(ModelPullProgress.Complete)
    }

    private suspend fun checkOllamaConnection(): Boolean {
        return checkUrl("${config.baseUrl}/api/tags")
    }

    private suspend fun checkUrl(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            } catch (_: Exception) {
                false
            }
        }
    }
    
    private fun Message.toOllamaMessage(): OllamaMessage {
        val role = when (role) {
            MessageRole.SYSTEM -> "system"
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
            MessageRole.TOOL -> "tool"
        }
        
        val toolCalls = if (toolCalls.isNotEmpty()) {
            toolCalls.map { tc ->
                OllamaToolCall(
                    function = OllamaFunctionCall(
                        name = tc.name,
                        arguments = tc.arguments.mapValues { (_, v) ->
                            JsonPrimitive(v.toString())
                        }
                    )
                )
            }
        } else null
        
        return OllamaMessage(
            role = role,
            content = content,
            toolCalls = toolCalls
        )
    }
}

sealed class LLMResponse {
    data class Content(
        val content: String,
        val toolCalls: List<ToolCall> = emptyList(),
        val done: Boolean = false,
        val totalDuration: Long? = null,
        val evalCount: Int? = null,
        val evalDuration: Long? = null,
        val loadDuration: Long? = null,
        val promptEvalCount: Int? = null,
        val promptEvalDuration: Long? = null
    ) : LLMResponse()
    
    data class Error(val message: String) : LLMResponse()
}

sealed class ModelPullProgress {
    data class Update(
        val status: String,
        val digest: String?,
        val total: Long?,
        val completed: Long?
    ) : ModelPullProgress()
    
    data object Complete : ModelPullProgress()
    data class Error(val message: String) : ModelPullProgress()
}

@Serializable
data class OllamaModelsResponse(
    val models: List<OllamaModel>
)

@Serializable
data class OllamaModel(
    val name: String,
    val model: String,
    val size: Long,
    val digest: String,
    val modifiedAt: String? = null,
    val source: ModelSource = ModelSource.OLLAMA,
    val downloadUrl: String? = null,
    val description: String? = null
)

@Serializable
private data class OllamaResponseJson(
    val message: OllamaResponseMessage? = null,
    val done: Boolean? = null,
    val totalDuration: Long? = null,
    val evalCount: Int? = null,
    val evalDuration: Long? = null,
    val loadDuration: Long? = null,
    val promptEvalCount: Int? = null,
    val promptEvalDuration: Long? = null,
    val error: String? = null
)

@Serializable
private data class OllamaResponseMessage(
    val role: String? = null,
    val content: String? = null,
    val toolCalls: List<OllamaToolCall>? = null
)

@Serializable
private data class OllamaGenerateResponseJson(
    val response: String? = null,
    val done: Boolean? = null,
    val totalDuration: Long? = null,
    val evalCount: Int? = null,
    val evalDuration: Long? = null,
    val loadDuration: Long? = null,
    val error: String? = null
)

@Serializable
private data class OllamaPullProgress(
    val status: String? = null,
    val digest: String? = null,
    val total: Long? = null,
    val completed: Long? = null,
    val done: Boolean? = null
)

private val huggingFaceRecommendedModels = listOf(
    OllamaModel(
        name = "Qwen2.5-1.5B-Instruct (Q4_K_M)",
        model = "qwen2.5-1.5b-instruct-q4_k_m",
        size = 1_100_000_000L,
        digest = "hf-qwen2.5-1.5b",
        source = ModelSource.HUGGING_FACE,
        downloadUrl = "https://huggingface.co/bartowski/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/Qwen2.5-1.5B-Instruct-Q4_K_M.gguf",
        description = "Fast instruction model suited for on-device assistants."
    ),
    OllamaModel(
        name = "Llama-3.2-3B-Instruct (Q4_K_M)",
        model = "llama-3.2-3b-instruct-q4_k_m",
        size = 2_200_000_000L,
        digest = "hf-llama3.2-3b",
        source = ModelSource.HUGGING_FACE,
        downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
        description = "Stronger reasoning and chat quality for capable phones."
    ),
    OllamaModel(
        name = "Phi-3.5-mini-instruct (Q4_K_M)",
        model = "phi-3.5-mini-instruct-q4_k_m",
        size = 2_400_000_000L,
        digest = "hf-phi3.5-mini",
        source = ModelSource.HUGGING_FACE,
        downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
        description = "Balanced model for coding and assistant-style responses."
    )
)

private val githubRecommendedModels = listOf(
    OllamaModel(
        name = "TinyLlama-1.1B-Chat (Q5_K_M)",
        model = "tinyllama-1.1b-chat-q5_k_m",
        size = 800_000_000L,
        digest = "gh-tinyllama-1.1b-q5",
        source = ModelSource.GITHUB,
        downloadUrl = "https://github.com/Mozilla-Ocho/llamafile/releases/download/0.8.17/TinyLlama-1.1B-Chat-v1.0.Q5_K_M.llamafile",
        description = "Low-memory assistant model for budget Android devices."
    ),
    OllamaModel(
        name = "Llama-3.2-3B-Instruct (Q6_K)",
        model = "llama-3.2-3b-instruct-q6_k",
        size = 2_700_000_000L,
        digest = "gh-llama-3.2-3b-q6",
        source = ModelSource.GITHUB,
        downloadUrl = "https://github.com/Mozilla-Ocho/llamafile/releases/download/0.8.17/Llama-3.2-3B-Instruct.Q6_K.llamafile",
        description = "Higher quality assistant model for capable Android hardware."
    )
)
