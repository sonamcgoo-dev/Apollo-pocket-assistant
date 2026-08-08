package com.appolopocket.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.*

/**
 * Serializer for Any type to handle dynamic JSON values
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("AnySerializer requires Json encoder")
        
        val jsonElement = when (value) {
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { it?.let { v -> anyToJsonElement(v) } ?: JsonNull })
            is Map<*, *> -> JsonObject(value.entries.filter { it.key is String }.associate { 
                it.key as String to (it.value?.let { v -> anyToJsonElement(v) } ?: JsonNull)
            })
            is Enum<*> -> JsonPrimitive(value.name)
            else -> JsonPrimitive(value.toString())
        }
        
        jsonEncoder.encodeJsonElement(jsonElement)
    }
    
    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("AnySerializer requires Json decoder")
        
        return jsonDecoder.decodeJsonElement().let { jsonToAny(it) } ?: Any()
    }
    
    private fun anyToJsonElement(value: Any): JsonElement = when (value) {
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Char -> JsonPrimitive(value.toString())
        is List<*> -> JsonArray(value.mapNotNull { it?.let { v -> anyToJsonElement(v) } })
        is Map<*, *> -> JsonObject(value.entries.filter { it.key is String }.associate { 
            it.key as String to (it.value?.let { v -> anyToJsonElement(v) } ?: JsonNull)
        })
        else -> JsonPrimitive(value.toString())
    }
    
    private fun jsonToAny(element: JsonElement): Any? = when (element) {
        is JsonPrimitive -> element.content
        is JsonArray -> element.map { jsonToAny(it) }
        is JsonObject -> element.toMap().mapValues { jsonToAny(it.value) }
        JsonNull -> null
    }
}
