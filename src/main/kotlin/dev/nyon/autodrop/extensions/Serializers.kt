package dev.nyon.autodrop.extensions

import com.mojang.brigadier.StringReader
import dev.nyon.autodrop.minecraft
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.world.item.Item

/*? if >=1.20.5 {*/
import kotlin.jvm.optionals.getOrNull
import net.minecraft.nbt.NbtOps
/*?}*/

object ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("item", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Item {
        val resourceLocation = resourceLocation(decoder.decodeString())!!
        return BuiltInRegistries.ITEM.get(resourceLocation)/*? if >=1.21.2 {*//*.get().value()*//*?}*/
    }

    override fun serialize(
        encoder: Encoder, value: Item
    ) {
        val resourceLocation = BuiltInRegistries.ITEM.getKey(value)
        encoder.encodeString(resourceLocation.toString())
    }
}

object DataComponentPatchSerializer : KSerializer<StoredComponents> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("components", PrimitiveKind.STRING)
    private val registryAccess = minecraft.connection?.registryAccess() ?: VanillaRegistries.createLookup()

    /*? if >=1.20.5 {*/
    private val itemParser = ItemParser(registryAccess)
    private val dynamicOps = registryAccess.createSerializationContext(NbtOps.INSTANCE)
    /*?}*/

    fun toPatch(decoded: String): StoredComponents {
        /*? if >=1.20.5 {*/
        val correctString = if (decoded.startsWith('[')) "stone$decoded" else decoded
        val result = itemParser.parse(StringReader(correctString))
        return result.components
        /*?} else {*/
        /*val correctString = if (decoded.startsWith('{')) "stone$decoded" else decoded
        val result = ItemParser.parseForItem(registryAccess.lookupOrThrow(Registries.ITEM), StringReader(correctString))
        return result.nbt ?: emptyStoredComponents
        *//*?}*/
    }

    @Suppress("UNCHECKED_CAST", "KotlinRedundantDiagnosticSuppress")
    fun toString(patch: StoredComponents): String {
        /*? if >=1.21 {*/
        val stringMap = patch.entrySet().mapNotNull { (type, value) ->
            val resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type)
            val encoded =
                (type.codec() as? com.mojang.serialization.Encoder<Any>)?.encodeStart(dynamicOps, value.orElseThrow())
                    ?.result()?.getOrNull()
            return@mapNotNull if (resourceLocation == null || encoded == null) null
            else "$resourceLocation=$encoded"
        }
        return stringMap.toString()
        /*?} else if >=1.20.5 {*/
        /*val stringMap = patch.mapNotNull { component ->
            val resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type)
            val encoded =
                (component.type.codec() as? com.mojang.serialization.Encoder<Any>)?.encodeStart(dynamicOps, component.value)
                    ?.result()?.getOrNull()
            return@mapNotNull if (resourceLocation == null || encoded == null) null
            else "$resourceLocation=$encoded"
        }
        return stringMap.toString()
        *//*?} else {*/
        /*return patch.asString
        *//*?}*/
    }

    override fun deserialize(decoder: Decoder): StoredComponents {
        val decoded = decoder.decodeString()
        return toPatch(decoded)
    }

    override fun serialize(encoder: Encoder, value: StoredComponents) {
        encoder.encodeString(toString(value))
    }
}