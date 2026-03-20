package dev.nyon.autodrop.mixins;

import dev.nyon.autodrop.AutoDrop;
import dev.nyon.autodrop.KeyBindings;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(
        method = "keyPressed",
        at = @At("HEAD")
    )
    private void triggerTriggerKey(
        KeyEvent keyEvent,
        CallbackInfoReturnable<Boolean> cir
    ) {
        KeyBindings.INSTANCE.getKeyBinds()
            .forEach((mapping, function) -> {
                if (mapping.matches(keyEvent)) function.invoke(AutoDrop.INSTANCE.getMinecraft());
            });
    }
}
