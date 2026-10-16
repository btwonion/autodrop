package dev.nyon.autodrop.config

import kotlinx.serialization.json.*

var config: Config = Config()
    internal set

const val CONFIG_VERSION = 3

/**
 * Converts older formats when Konfig requests a migration. Konfig only asks after the current format failed to
 * decode, so unknown keys must keep failing: that is what routes old archives here instead of to defaults.
 *
 * @param json is the serializer configured by Konfig.
 * @param jsonTree is the config content supplied by Konfig.
 * @param version is the stored config version, or null for the original unversioned format.
 * @return the migrated config, or null if Konfig should apply its default config.
 */
internal fun migrate(json: Json, jsonTree: JsonElement, version: Int?): Config? = runCatching {
    when (version) {
        null, 1, 2 -> migrateArchives(json, jsonTree, version)
        else -> null
    }
}.getOrNull()

private fun migrateArchives(json: Json, jsonTree: JsonElement, version: Int?): Config? {
    val root = jsonTree.jsonObject
    val profiles = root["archives"]?.jsonArray?.map { element ->
        val old = element.jsonObject
        val rules = if (version == 2) {
            old["entries"]!!.jsonArray.map { entry ->
                val rule = entry.jsonObject
                ProfileRule(
                    type = legacyType(rule["type"]),
                    predicate = rule["predicate"]?.jsonPrimitive?.contentOrNull ?: "",
                    quantityMode = if (rule["dropEverything"]?.jsonPrimitive?.booleanOrNull != false) QuantityMode.ALL else QuantityMode.HALF, // The old per-stack minimum becomes an inventory-wide minimum; 1 never restricted anything.
                    minimumInventory = (rule["amount"]?.jsonPrimitive?.intOrNull ?: 1).takeIf { it > 1 } ?: 0)
            }
        } else {
            old["items"]!!.jsonArray.map { ProfileRule(type = legacyType(it)) }
        }
        Profile(
            enabled = old["enabled"]?.jsonPrimitive?.booleanOrNull ?: true,
            name = old["name"]!!.jsonPrimitive.content,
            entries = rules.toMutableList(),
            protectedSlots = (old[if (version == 2) "ignoredSlots" else "lockedSlots"]?.jsonArray
                ?: JsonArray(emptyList())).map { it.jsonPrimitive.int }.toMutableSet()
        )
    }?.toMutableList() ?: return null
    return Config(
        enabled = root["enabled"]?.jsonPrimitive?.booleanOrNull ?: true,
        triggerConfig = root["triggerConfig"]?.let { json.decodeFromJsonElement<TriggerConfig>(it) } ?: TriggerConfig(),
        profiles = profiles,
        dropDelay = root["dropDelay"]?.jsonPrimitive?.longOrNull ?: 200)
}

private fun legacyType(value: JsonElement?): String? {
    val id = value?.jsonPrimitive?.contentOrNull ?: return null
    if (id == "air" || id == "minecraft:air" || id.isBlank()) return null
    return if (':' in id) id else "minecraft:$id"
}
