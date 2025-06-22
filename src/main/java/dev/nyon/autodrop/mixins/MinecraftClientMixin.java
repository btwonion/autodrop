package dev.nyon.autodrop.mixins;

import dev.nyon.autodrop.KeyBindings;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    public void onTick(CallbackInfo ci) {
        KeyBindings.INSTANCE.handleKeybindings((Minecraft) (Object) this);
    }
}