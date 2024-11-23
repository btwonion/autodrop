package dev.nyon.autodrop.mixins;

import dev.nyon.autodrop.AutoDrop;
import dev.nyon.autodrop.config.ConfigHandlerKt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(
        method = "add(ILnet/minecraft/world/item/ItemStack;)Z",
        at = @At(value = "RETURN")
    )
    public void invokeOnTake(
        int slot,
        ItemStack stack,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (ConfigHandlerKt.getConfig()
            .getTriggerConfig()
            .getOnPickup()) AutoDrop.INSTANCE.invokeAutodrop();
    }

    @Inject(
        method = "setItem",
        at = @At(value = "RETURN")
    )
    public void invokeOnTake(
        int i,
        ItemStack itemStack,
        CallbackInfo ci
    ) {
        if (ConfigHandlerKt.getConfig()
            .getTriggerConfig()
            .getOnPickup()) AutoDrop.INSTANCE.invokeAutodrop();
    }

    @Inject(
        method = "addResource(ILnet/minecraft/world/item/ItemStack;)I",
        at = @At("RETURN")
    )
    public void invokeOnResourceTake(
        int slot,
        ItemStack stack,
        CallbackInfoReturnable<Integer> cir
    ){
        if (ConfigHandlerKt.getConfig()
            .getTriggerConfig()
            .getOnPickup()) AutoDrop.INSTANCE.invokeAutodrop();
    }
}
