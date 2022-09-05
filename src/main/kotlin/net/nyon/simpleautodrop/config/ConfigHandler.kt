package net.nyon.simpleautodrop.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.io.File

val config = run {
    val file = File(FabricLoader.getInstance().configDir.toAbsolutePath().toString(), "simpleautodrop.json")
    file.createNewFile()
    return@run file
}
private val json = Json { prettyPrint = true }

fun saveConfig() {
    config.writeBytes(json.encodeToString(settings).encodeToByteArray())
}

fun loadConfig() {
    if (config.readBytes().isEmpty()) saveConfig()
    settings = json.decodeFromString(config.readBytes().toString())
}