package com.example.quitesmoking.model

import kotlinx.serialization.Serializable

/**
 * Root config that mirrors your JSON shape.
 * - questionMap: map of question-id -> QuestionDef
 * - routing: map of question-id -> RouteDef (custom serializer handles null/string/object)
 */
@Serializable
data class OnboardingConfig(
    val version: Int,
    val questionMap: Map<String, QuestionDef>,
    val routing: Map<String, @Serializable(with = RouteDefKSerializer::class) RouteDef>
)

/** Question node. `type` is kept as String to match your JSON exactly. */
@Serializable
data class QuestionDef(
    val question: String,
    val type: String,              // "textInput", "singleChoice", "multiChoice", "datePicker", "imageCapture"
    val options: List<String>? = null
)

/** Simple bag for answers keyed by question id (e.g., "2" -> "🙋‍♂️ Male"). */
typealias AnswerBag = MutableMap<String, Any?>

/**
 * Routing sealed type:
 * - End: finish (null in JSON)
 * - Next("X"): jump to id "X" (string in JSON)
 * - Rules(map, default): JSON object { "contains:xxx": "id", ..., "default": "id?" }
 */
sealed class RouteDef {
    data object End : RouteDef()
    data class Next(val id: String) : RouteDef()
    data class Rules(
        val rules: Map<String, String>, // keys like "contains:self-describe"
        val default: String?            // may be null (→ End)
    ) : RouteDef()
}
