package dev.nyon.autodrop.extensions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

object ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("item", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Item {
        val resourceLocation = resourceLocation(decoder.decodeString())!!
        return BuiltInRegistries.ITEM.get(resourceLocation)/*? if >=1.21.2 {*/.get().value()/*?}*/
    }

    override fun serialize(
        encoder: Encoder, value: Item
    ) {
        val resourceLocation = BuiltInRegistries.ITEM.getKey(value)
        encoder.encodeString(resourceLocation.toString())
    }
}
