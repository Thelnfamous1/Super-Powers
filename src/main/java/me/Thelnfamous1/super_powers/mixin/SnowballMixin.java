package me.Thelnfamous1.super_powers.mixin;

import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public abstract class SnowballMixin extends ThrowableItemProjectile {

    protected SnowballMixin(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), cancellable = true)
    private void handleOnHitEntity(EntityHitResult pResult, CallbackInfo ci){
        if(this.getTags().contains(SuperpowerHelper.ICE_SUPERPOWER_TAG)){
            ci.cancel();
            Entity entity = pResult.getEntity();
            SuperpowerHelper.applyFreezeDamage(entity, 1);
        }
    }

}
