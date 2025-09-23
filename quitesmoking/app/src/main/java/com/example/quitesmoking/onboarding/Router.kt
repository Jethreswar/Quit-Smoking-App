package com.example.quitesmoking.onboarding

import com.example.quitesmoking.model.AnswerBag
import com.example.quitesmoking.model.OnboardingConfig
import com.example.quitesmoking.model.RouteDef
import java.text.Normalizer

/**
 * Decide the next question ID from:
 * - current question ID
 * - config.routing entry
 * - current answers
 */
fun nextIdFor(
    currentId: String,
    config: OnboardingConfig,
    answers: AnswerBag
): String? {
    val route = config.routing[currentId] ?: return null
    return when (route) {
        is RouteDef.End  -> null
        is RouteDef.Next -> route.id
        is RouteDef.Rules -> {
            val haystack = normalizeAnswer(answers[currentId])
            // Evaluate rule keys in insertion order; first match wins.
            for ((key, targetId) in route.rules) {
                val opAndVal = key.split(":", limit = 2)
                if (opAndVal.size != 2) continue
                val op = opAndVal[0].trim().lowercase()
                val valRaw = opAndVal[1].trim()
                val needle = normalizeText(valRaw)

                val matched = when (op) {
                    "contains"     -> haystack.contains(needle)
                    // Optional future operators you can use in configs later:
                    "equals", "eq" -> haystack == needle
                    "notcontains"  -> !haystack.contains(needle)
                    else           -> false
                }
                if (matched) return targetId
            }
            route.default // may be null -> finish
        }
    }
}

/** Turn an answer (String or List<String>) into a normalized search haystack. */
private fun normalizeAnswer(value: Any?): String = when (value) {
    is String -> normalizeText(value)
    is List<*> -> normalizeText(value.joinToString(" ") { it?.toString() ?: "" })
    is Boolean, is Number -> normalizeText(value.toString())
    else -> ""
}

/** Strip emojis/diacritics/punct, lowercase, keep alnum+spaces so "✅ Yes" -> "yes" */
private fun normalizeText(input: String): String {
    val decomp = Normalizer.normalize(input, Normalizer.Form.NFD)
    // remove diacritics
    val noMarks = decomp.replace("\\p{Mn}+".toRegex(), "")
    // convert non-alphanumeric to space, collapse spaces, lowercase
    return noMarks
        .replace("[^A-Za-z0-9]+".toRegex(), " ")
        .trim()
        .lowercase()
}
