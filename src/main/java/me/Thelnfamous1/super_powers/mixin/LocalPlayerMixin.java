package me.Thelnfamous1.super_powers.mixin;

import me.Thelnfamous1.super_powers.client.ClientHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"))
    private void handleAiStep(CallbackInfo ci){
        ClientHandler.tickInput((LocalPlayer) (Object)this);
    }
}
