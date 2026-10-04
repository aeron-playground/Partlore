package dev.partlore.tools.content.load

import tools.jackson.databind.JsonNode

// The schema has already checked types; these just read them.
internal fun JsonNode.text(name: String): String = get(name).asString()

internal fun JsonNode.textOrNull(name: String): String? = get(name)?.takeUnless { it.isNull }?.asString()

internal fun JsonNode.int(name: String): Int = get(name).intValue()

internal fun JsonNode.intOrNull(name: String): Int? = get(name)?.takeUnless { it.isNull }?.intValue()

internal fun JsonNode.doubleOrNull(name: String): Double? = get(name)?.takeUnless { it.isNull }?.doubleValue()

internal fun JsonNode.boolOrNull(name: String): Boolean? = get(name)?.takeUnless { it.isNull }?.booleanValue()

internal fun JsonNode.items(name: String): List<JsonNode> = get(name)?.values()?.toList().orEmpty()

internal fun JsonNode.texts(name: String): List<String> = items(name).map { it.asString() }
