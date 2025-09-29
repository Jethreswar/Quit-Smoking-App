package com.example.quitesmoking.onboarding

import org.json.JSONArray
import org.json.JSONObject

object OnboardingJson {
    fun toJsonString(map: Map<String, Any?>, pretty: Boolean = true): String {
        val obj = JSONObject(map.mapValues { anyToJsonValue(it.value) })
        return if (pretty) obj.toString(2) else obj.toString()
    }

    private fun anyToJsonValue(v: Any?): Any? = when (v) {
        null -> JSONObject.NULL
        is Map<*, *> -> JSONObject(v.mapKeys { it.key.toString() }.mapValues { anyToJsonValue(it.value) })
        is List<*>  -> JSONArray(v.map { anyToJsonValue(it) })
        is Boolean, is Number, is String -> v
        else -> v.toString()
    }
}
