package dev.nyon.simpleautodrop.mixins;

import dev.nyon.simpleautodrop.SimpleAutoDrop;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        SimpleAutoDrop.INSTANCE.tick((Minecraft) (Object) this);
    }
}
