package com.example.quitesmoking.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Handles three JSON shapes:
 * - null        -> RouteDef.End
 * - "8.1.1"     -> RouteDef.Next("8.1.1")
 * - { ... }     -> RouteDef.Rules(rules=object minus "default", default=object["default"])
 */
object RouteDefKSerializer : KSerializer<RouteDef> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("RouteDef")

    override fun serialize(encoder: Encoder, value: RouteDef) {
        val output = encoder as? JsonEncoder
            ?: error("RouteDefKSerializer works with JSON only")
        val json = when (value) {
            is RouteDef.End -> JsonNull
            is RouteDef.Next -> JsonPrimitive(value.id)
            is RouteDef.Rules -> {
                val content = buildMap<String, JsonElement> {
                    value.rules.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
                    value.default?.let { put("default", JsonPrimitive(it)) }
                }
                JsonObject(content)
            }
        }
        output.encodeJsonElement(json)
    }

    override fun deserialize(decoder: Decoder): RouteDef {
        val input = decoder as? JsonDecoder
            ?: error("RouteDefKSerializer works with JSON only")
        val el = input.decodeJsonElement()
        return when (el) {
            is JsonNull -> RouteDef.End
            is JsonPrimitive -> {
                if (el.isString) RouteDef.Next(el.content) else RouteDef.End
            }
            is JsonObject -> {
                val map = el.mapValues { (_, v) -> v.jsonPrimitive.content }
                val default = map["default"]
                val rules = map.filterKeys { it != "default" }
                if (default == null && rules.isEmpty()) RouteDef.End
                else RouteDef.Rules(rules = rules, default = default)
            }
            else -> RouteDef.End
        }
    }
}
