package dev.nyon.autodrop.mixins;

import dev.nyon.autodrop.AutoDrop;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (!player.getInventory().equals((PlayerInventory) (Object) this)) return;
        AutoDrop.INSTANCE.onTake();
    }

    @Inject(method = "setStack", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfo ci) {
        if (!player.getInventory().equals((PlayerInventory) (Object) this)) return;
        AutoDrop.INSTANCE.onTake();
    }
}
