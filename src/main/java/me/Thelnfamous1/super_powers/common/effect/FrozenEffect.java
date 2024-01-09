package me.Thelnfamous1.super_powers.common.effect;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

public class FrozenEffect extends MobEffect implements CustomEffectParticle{
    public FrozenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "3b79b26f-da85-4601-91a2-c9453a19be8d",
                -0.15F,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(!pLivingEntity.level.isClientSide && pLivingEntity.canFreeze())
            // LivingEntity#aiStep removes frost at a rate of 2 ticks per tick, so add 2 extra ticks to ensure frozen
            pLivingEntity.setTicksFrozen(pLivingEntity.getTicksRequiredToFreeze() + 2);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
        this.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration >= 1;
    }

    @Override
    public ParticleOptions getCustomParticle() {
        return SuperPowers.SNOWFLAKE.get();
    }
}
