package dev.nyon.simpleautodrop.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.item.Item

class ItemSerializer : KSerializer<Item> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("item", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Item) {
        encoder.encodeInt(Item.getId(value))
    }

    override fun deserialize(decoder: Decoder): Item {
        return Item.byId(decoder.decodeInt())
    }
}