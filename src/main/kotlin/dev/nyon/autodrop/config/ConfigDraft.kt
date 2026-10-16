package dev.nyon.autodrop.config

/**
 * Represents the shared draft for one config editing session.
 *
 * @param source is the saved config copied independently for editing and discarding changes.
 */
class ConfigDraft(source: Config) {
    var value: Config = source.deepCopy()
        private set
    private val original = source.deepCopy()
    val dirty: Boolean get() = value != original
    fun discard() { value = original.deepCopy() }
    /** Whether the saved config already contains [predicate], so it was accepted when that config was saved. */
    fun isSavedPredicate(predicate: String) = original.profiles.any { profile -> profile.entries.any { it.predicate == predicate } }
}

/**
 * Represents a config validation error shown before saving the draft.
 *
 * @param key identifies the translated error message.
 * @param detail provides the profile name or predicate error for that message.
 */
data class ValidationIssue(val key: String, val detail: String = "")

private val RULE_TYPE = Regex("[a-z0-9_.-]+:[a-z0-9/._-]+")

/** Whether [type] is a full registry ID a rule may store; air never names an item to match. */
fun isValidRuleType(type: String) = type.matches(RULE_TYPE) && type != "minecraft:air"

fun validateConfig(value: Config, predicateError: (String) -> String? = { null }): ValidationIssue? {
    if (value.dropDelay !in 0..Int.MAX_VALUE.toLong()) return ValidationIssue("invalid.delay")
    value.profiles.forEach { profile ->
        if (profile.name.isBlank()) return ValidationIssue("invalid.name")
        if (profile.enabled && profile.mode == ProfileMode.KEEP) {
            if (profile.entries.isEmpty()) return ValidationIssue("invalid.empty_keep", profile.name)
            if (!profile.keepReviewed) return ValidationIssue("invalid.keep_review", profile.name)
        }
        profile.entries.forEach { rule ->
            if (rule.type?.let { !isValidRuleType(it) } == true) return ValidationIssue("invalid.type", profile.name)
            if (rule.quantity <= 0 || rule.minimumInventory < 0) return ValidationIssue("invalid.quantity", profile.name)
            predicateError(rule.predicate)?.let { return ValidationIssue("invalid.predicate", "${profile.name}: $it") }
        }
    }
    return null
}
