package com.example.quitesmoking.onboarding

import kotlinx.serialization.json.*

fun answersToJsonString(answers: Map<String, Any?>, pretty: Boolean = true): String {
    fun toElem(v: Any?): JsonElement = when (v) {
        null -> JsonNull
        is String -> JsonPrimitive(v)
        is Number -> JsonPrimitive(v)
        is Boolean -> JsonPrimitive(v)
        is List<*> -> JsonArray(v.map { toElem(it) })
        is Map<*, *> -> JsonObject(v.mapKeys { it.key.toString() }.mapValues { toElem(it.value) })
        else -> JsonPrimitive(v.toString())
    }
    val obj = JsonObject(answers.mapValues { toElem(it.value) })
    val json = if (pretty) Json { prettyPrint = true } else Json
    return json.encodeToString(JsonObject.serializer(), obj)
}
