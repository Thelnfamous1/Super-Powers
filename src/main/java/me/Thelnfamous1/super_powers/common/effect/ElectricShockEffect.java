package me.Thelnfamous1.super_powers.common.effect;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class ElectricShockEffect extends InstantenousMobEffect implements CustomEffectParticle{

    public static final int SHOCK_DAMAGE = 4;

    public ElectricShockEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.hurt(DamageSource.LIGHTNING_BOLT, (float)(SHOCK_DAMAGE << pAmplifier));
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
        int damage = (int)(pHealth * (double)(SHOCK_DAMAGE << pAmplifier) + 0.5D);
        pLivingEntity.hurt(DamageSource.LIGHTNING_BOLT, (float)damage);
    }

    @Override
    public ParticleOptions getCustomParticle() {
        return SuperPowers.ELECTRIC_SHOCK_PARTICLE.get();
    }
}
