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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    public void onTake(int i, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        SimpleAutoDrop.INSTANCE.onTake(player, (Inventory) (Object) this, false);
    }

}
