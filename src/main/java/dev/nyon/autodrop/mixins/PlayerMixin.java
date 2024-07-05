package dev.nyon.autodrop.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nyon.autodrop.AutoDrop;
import dev.nyon.autodrop.config.ConfigHandlerKt;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(
        method = "jumpFromGround",
        at = @At("HEAD")
    )
    private void invokeOnJump(CallbackInfo ci) {
        if (ConfigHandlerKt.getConfig()
            .getTriggerConfig()
            .getOnJump()) AutoDrop.INSTANCE.invokeAutodrop();
    }

    @WrapOperation(
        method = "updatePlayerPose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setPose(Lnet/minecraft/world/entity/Pose;)V"
        )
    )
    private void invokeOnCrouch(
        Player instance,
        Pose pose,
        Operation<Void> original
    ) {
        if (ConfigHandlerKt.getConfig()
            .getTriggerConfig()
            .getOnSneak() && pose == Pose.CROUCHING) AutoDrop.INSTANCE.invokeAutodrop();
        original.call(instance, pose);
    }
}
