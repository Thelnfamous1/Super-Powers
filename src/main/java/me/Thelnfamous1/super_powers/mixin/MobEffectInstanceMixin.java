package me.Thelnfamous1.super_powers.mixin;

import me.Thelnfamous1.super_powers.common.effect.CustomEffectParticle;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {

    @Shadow
    int duration;

    @Shadow @Final private MobEffect effect;

    @Inject(method = "tick", at = @At("HEAD"))
    private void handleTick(LivingEntity pEntity, Runnable pOnExpirationRunnable, CallbackInfoReturnable<Boolean> cir){
        if(this.duration > 0 && this.effect instanceof CustomEffectParticle cep){
            SuperpowerHelper.tickParticles(pEntity, cep.getCustomParticle(), null);
        }
    }
}
