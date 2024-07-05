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
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import kotlin.jvm.optionals.getOrNull

object ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("item", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Item {
        val resourceLocation = /*? if >=1.21 {*/ ResourceLocation.parse(decoder.decodeString()) /*?} else {*/ /*ResourceLocation(decoder.decodeString()) *//*?}*/
        return BuiltInRegistries.ITEM.get(resourceLocation)
    }

    override fun serialize(
        encoder: Encoder, value: Item
    ) {
        val resourceLocation = BuiltInRegistries.ITEM.getKey(value)
        encoder.encodeString(resourceLocation.toString())
    }
}

object DataComponentPatchSerializer : KSerializer<DataComponentPatch> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("components", PrimitiveKind.STRING)
    private val registryAccess = minecraft.connection?.registryAccess() ?: VanillaRegistries.createLookup()
    private val itemParser = ItemParser(registryAccess)
    private val dynamicOps = registryAccess.createSerializationContext(NbtOps.INSTANCE)

    override fun deserialize(decoder: Decoder): DataComponentPatch {
        val result = itemParser.parse(StringReader(decoder.decodeString()))
        return result.components
    }

    override fun serialize(encoder: Encoder, value: DataComponentPatch) {
        val stringMap = value.entrySet().mapNotNull { (type, value) ->
            val resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type)
            val encoded = type.codec()!!.encodeStart(dynamicOps, value.get() as Nothing?).result().getOrNull()
            return@mapNotNull if (resourceLocation == null || encoded == null) null
            else "$resourceLocation=$encoded"
        }

        encoder.encodeString(stringMap.toString())
    }
}