package com.example.quitesmoking.utils

import android.content.Context
import androidx.annotation.RawRes
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.nio.charset.StandardCharsets

/**
 * Lenient, resilient JSON parser used app-wide.
 * - ignoreUnknownKeys: future-proof against extra fields (Firestore, A/B variants)
 * - isLenient: allows comments/trailing commas during local testing if needed
 * - explicitNulls=false: omits nulls when encoding; treats absent/null similarly when decoding
 */
val JsonLenient: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = false
    explicitNulls = false
}

/** Read a raw resource into a UTF-8 String (no BOM). */
fun Context.readRawText(@RawRes resId: Int): String =
    resources.openRawResource(resId).bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
