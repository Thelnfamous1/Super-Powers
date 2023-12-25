package me.Thelnfamous1.super_powers.common.util;

import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SuperpowerHelper {

    public static final String ICE_SUPERPOWER_TAG = "%s:ice";
    public static final int USE_BONEMEAL_EVENT_ID = 1505;
    public static final int SHOOT_FIREBALL_EVENT_ID = 1018;

    public static void castNone(Entity ignoredShooter){
    }

    public static void fireBeam(LivingEntity shooter){
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            cap.setFiringBeam(true);
            EnergyBeam energyBeam = new EnergyBeam(shooter.level, shooter);
            shooter.level.addFreshEntity(energyBeam);
        });
    }

    public static void castBonemeal(LivingEntity shooter){
        HitResult hitResult = getHitResult(shooter);
        if(hitResult instanceof BlockHitResult blockHitResult){
            Level level = shooter.level;
            BlockPos targetPos = blockHitResult.getBlockPos();
            BlockPos relativeTargetPos = targetPos.relative(blockHitResult.getDirection());
            ItemStack boneMeal = Items.BONE_MEAL.getDefaultInstance();
            if (shooter instanceof Player player ? BoneMealItem.applyBonemeal(boneMeal, level, targetPos, player) : BoneMealItem.growCrop(boneMeal, level, targetPos)) {
                if (!level.isClientSide) {
                    level.levelEvent(USE_BONEMEAL_EVENT_ID, targetPos, 0);
                }
            } else {
                BlockState targetState = level.getBlockState(targetPos);
                boolean faceSturdy = targetState.isFaceSturdy(level, targetPos, blockHitResult.getDirection());
                if (faceSturdy && BoneMealItem.growWaterPlant(boneMeal, level, relativeTargetPos, blockHitResult.getDirection())) {
                    if (!level.isClientSide) {
                        level.levelEvent(USE_BONEMEAL_EVENT_ID, relativeTargetPos, 0);
                    }
                }
            }
        }
    }

    public static void castSnowball(LivingEntity shooter){
        Snowball snowball = new Snowball(shooter.level, shooter);
        snowball.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0, 1.5F, 0.0F);
        snowball.getTags().add(ICE_SUPERPOWER_TAG);
        shooter.level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (shooter.level.getRandom().nextFloat() * 0.4F + 0.8F));
        shooter.level.addFreshEntity(snowball);
    }

    public static void castFireball(LivingEntity shooter){
        HitResult hitResult = getHitResult(shooter);
        Vec3 target = hitResult.getLocation();
        double xDist = target.x - shooter.getX();
        double yDist = target.y - shooter.getY();
        double zDist = target.z - shooter.getZ();
        SmallFireball fireball = new SmallFireball(shooter.level, shooter, xDist, yDist, zDist);
        fireball.setPos(fireball.getX(), shooter.getEyeY() - 0.1D, fireball.getZ()); // similar positioning to Snowball
        shooter.level.levelEvent(null, SHOOT_FIREBALL_EVENT_ID, shooter.blockPosition(), 0);
        shooter.level.addFreshEntity(fireball);
    }


    public static void castLightning(Entity shooter){
        HitResult hitResult = getHitResult(shooter);
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(shooter.level);
        if(lightningBolt != null){
            Vec3 target = hitResult.getLocation();
            lightningBolt.moveTo(target.x, target.y, target.z, shooter.getYRot(), lightningBolt.getXRot()); // bolts can only be vertical, so use its default x rot
            if(shooter instanceof ServerPlayer player) lightningBolt.setCause(player);
            shooter.level.addFreshEntity(lightningBolt);
        }
    }

    public static HitResult getHitResult(Entity shooter){
        Vec3 from = shooter.getEyePosition();
        Vec3 to = from.add(shooter.getLookAngle().scale(1024));
        HitResult hitresult = shooter.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (hitresult.getType() != HitResult.Type.MISS) {
            to = hitresult.getLocation();
        }
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter.level, shooter, from, to, shooter.getBoundingBox().expandTowards(to).inflate(1.0D), target -> canHitEntity(shooter, target));
        if (entityHitResult != null) {
            hitresult = entityHitResult;
        }
        return hitresult;
    }

    public static boolean canHitEntity(Entity shooter, Entity target) {
        if (!target.isSpectator() && target.isAlive() && target.isPickable()) {
            return !shooter.isPassengerOfSameVehicle(target);
        } else {
            return false;
        }
    }

    public static void tickBeam(Entity shooter, Superpower superpower, int ticksFiringBeam) {
        if(ticksFiringBeam % 10 == 0){
            HitResult hitResult = getHitResult(shooter);
            if(hitResult instanceof EntityHitResult entityHitResult){
                Entity target = entityHitResult.getEntity();
                switch (superpower){
                    case LIGHTNING -> {
                        target.invulnerableTime = 0;
                        target.hurt(DamageSource.LIGHTNING_BOLT, 4.0F);
                    }
                    case FIRE -> {
                        target.setSecondsOnFire(5); // same as small fireball
                    }
                    case ICE -> {
                        target.setTicksFrozen(600); // decays at 2 ticks per tick, so we need double the desired ticks of 300
                    }
                }
            }
            else if(hitResult instanceof BlockHitResult blockHitResult){
                BlockPos blockPos = blockHitResult.getBlockPos();
                switch (superpower){
                    case FIRE -> {
                        BlockState blockState = shooter.level.getBlockState(blockPos);
                        if(!blockState.isAir() && !blockState.getMaterial().isLiquid()){
                            shooter.level.gameEvent(shooter, GameEvent.BLOCK_PLACE, blockPos);
                            shooter.level.setBlockAndUpdate(blockPos, Blocks.LAVA.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    public static void applyFreezeDamage(Entity entity, int base) {
        if(entity.canFreeze()){
            entity.hurt(DamageSource.FREEZE, entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES) ? 5 * base : base);
        }
    }
}
