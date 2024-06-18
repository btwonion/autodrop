import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant

plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}

private data class Field(val name: String, val value: String, val inline: Boolean)

private data class Embed(
    val title: String, val description: String, val timestamp: String, val color: Int, val fields: List<Field>
)

private data class DiscordWebhook(
    val username: String, val avatarUrl: String, val embeds: List<Embed>
)

tasks.register("postUpdate") {
    group = "mod"

    val version = project(stonecutter.versions.first().project).version.toString()
    val hyphenCount = version.count { it == '-' }
    val featureVersion = when (hyphenCount) {
        1 -> version.split("-").first()
        2 -> {
            val split = version.split("-")
            if (split.last().contains("rc") || split.last().contains("pre")) split.first()
            else "${split.first()}-${split[1]}"
        }
        3 -> {
            val split = version.split("-")
            "${split.first()}-${split[1]}"
        }
        else -> return@register
    }

    val url = providers.environmentVariable("DISCORD_WEBHOOK").orNull ?: return@register
    val roleId = providers.environmentVariable("DISCORD_ROLE_ID").orNull ?: return@register
    val changelogText = rootProject.file("changelog.md").readText()
    val webhook = DiscordWebhook(
        username = "${rootProject.name} Release Notifier",
        avatarUrl = "https://raw.githubusercontent.com/btwonion/autodrop/main/src/main/resources/assets/autodrop/icon/icon.png",
        embeds = listOf(
            Embed(
                title = "v$featureVersion of ${rootProject.name} released!",
                description = "# Changelog\n$changelogText",
                timestamp = Instant.now().toString(),
                color = 0x4ab616,
                fields = listOf(
                    Field(
                        "Supported versions", stonecutter.versions.joinToString { it.version }, false
                    ),
                    Field("Modrinth", "https://modrinth.com/mod/autodrop", true),
                    Field("GitHub", "https://github.com/btwonion/autodrop", true)
                )
            )
        )
    )

    @OptIn(ExperimentalSerializationApi::class)
    val embedsJson = buildJsonArray {
        webhook.embeds.map { embed ->
            add(buildJsonObject {
                put("title", embed.title)
                put("description", embed.description)
                put("timestamp", embed.timestamp)
                put("color", embed.color)
                putJsonArray("fields") {
                    addAll(embed.fields.map { field ->
                        buildJsonObject {
                            put("name", field.name)
                            put("value", field.value)
                            put("inline", field.inline)
                        }
                    })
                }
            })
        }
    }

    val json = buildJsonObject {
        put("username", webhook.username)
        put("avatar_url", webhook.avatarUrl)
        put("content", "<@$roleId>")
        put("embeds", embedsJson)
    }

    val jsonString = Json.encodeToString(json)
    HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonString)).build(), HttpResponse.BodyHandlers.ofString()
    )
}
