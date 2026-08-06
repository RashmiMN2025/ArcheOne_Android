package com.archeGlobal.one.network

import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Extracts a user-facing message from an API error body. Handles the FastAPI validation
 * shape (`detail: [{msg, loc, type, ...}]`), a plain string `detail`, an object `detail`,
 * and a `detail` that is itself a JSON-encoded string (e.g. upstream gateway errors like
 * `{"detail":"{\"msg\":\"...\",\"error\":\"...\"}"}`).
 */
fun parseApiErrorMessage(errorBody: String?, fallback: String): String {
    if (errorBody.isNullOrBlank()) return fallback
    return try {
        val root = JsonParser.parseString(errorBody).asJsonObject
        val detail = root.get("detail") ?: return fallback
        extractMessage(detail) ?: fallback
    } catch (_: Exception) {
        Regex("\"detail\"\\s*:\\s*\"([^\"]*)\"").find(errorBody)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")
            ?: fallback
    }
}

private fun extractMessage(element: JsonElement): String? {
    return when {
        element.isJsonArray -> {
            val messages = element.asJsonArray.mapNotNull { item ->
                if (item.isJsonObject) {
                    val obj = item.asJsonObject
                    obj.stringOrNull("message") ?: obj.stringOrNull("msg")
                } else {
                    null
                }
            }
            messages.takeIf { it.isNotEmpty() }?.joinToString("\n")
        }
        element.isJsonObject -> {
            val obj = element.asJsonObject
            obj.stringOrNull("msg") ?: obj.stringOrNull("message") ?: obj.stringOrNull("error")
        }
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
            val raw = element.asString
            val nested = try {
                JsonParser.parseString(raw)
            } catch (_: Exception) {
                null
            }
            if (nested != null && (nested.isJsonObject || nested.isJsonArray)) {
                extractMessage(nested) ?: raw.takeIf { it.isNotBlank() }
            } else {
                raw.takeIf { it.isNotBlank() }
            }
        }
        else -> null
    }
}

private fun com.google.gson.JsonObject.stringOrNull(key: String): String? {
    val value = this.get(key) ?: return null
    if (!value.isJsonPrimitive) return null
    return value.asString.takeIf { it.isNotBlank() }
}
