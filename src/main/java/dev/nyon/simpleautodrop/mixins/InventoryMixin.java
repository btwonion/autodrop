package dev.nyon.simpleautodrop.mixins;

import dev.nyon.simpleautodrop.SimpleAutoDrop;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (!player.getInventory().equals((Inventory) (Object) this)) return;
        SimpleAutoDrop.INSTANCE.onTake();
    }

    @Inject(method = "setItem", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfo ci) {
        if (!player.getInventory().equals((Inventory) (Object) this)) return;
        SimpleAutoDrop.INSTANCE.onTake();
    }
}
