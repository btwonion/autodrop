package dev.nyon.autodrop.mixins;

import dev.nyon.autodrop.AutoDrop;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    public void onTake(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        AutoDrop.INSTANCE.onTake();
    }

    @Inject(method = "setItem", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfo ci) {
        AutoDrop.INSTANCE.onTake();
    }
}
