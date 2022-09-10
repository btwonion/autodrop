package dev.nyon.simpleautodrop.mixins;

import dev.nyon.simpleautodrop.SimpleAutoDrop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleTakeItemEntity", at = @At(value = "RETURN"))
    public void onServerTake(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        SimpleAutoDrop.INSTANCE.onTake(player, player.getInventory(), true);
    }

}
