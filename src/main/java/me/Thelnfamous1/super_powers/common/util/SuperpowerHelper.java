package me.Thelnfamous1.super_powers.common.util;

import com.mojang.datafixers.util.Either;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;

public class SuperpowerHelper {

    public static final String ICE_SUPERPOWER_TAG = "%s:ice";
    public static final int USE_BONEMEAL_EVENT_ID = 1505;
    public static final int SHOOT_FIREBALL_EVENT_ID = 1018;
    public static final double HIT_DISTANCE = 1024D;

    public static void castNone(Entity ignoredShooter){
    }

    public static void noTick(Entity ignoredShooter, Either<EntityHitResult, BlockHitResult> ignoredHitResultEither, int ignoredTicksFiringBeam){
    }

    public static void fireBeam(LivingEntity shooter){
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> fireBeamRaw(shooter, cap));
    }

    private static void fireBeamRaw(LivingEntity shooter, SuperpowerCapabilityInterface cap) {
        cap.setFiringBeam(true);
        EnergyBeam energyBeam = new EnergyBeam(shooter.level, shooter);
        shooter.level.addFreshEntity(energyBeam);
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
        Vec3 start = shooter.getEyePosition(1.0F);
        HitResult pick = shooter.pick(HIT_DISTANCE, 1.0F, false);
        double distSqr = Mth.square(HIT_DISTANCE);
        if (pick.getType() != HitResult.Type.MISS) {
            distSqr = pick.getLocation().distanceToSqr(start);
        }

        Vec3 viewVector = shooter.getViewVector(1.0F);
        Vec3 to = start.add(viewVector.scale(HIT_DISTANCE));
        AABB searchBox = shooter.getBoundingBox().expandTowards(viewVector.scale(HIT_DISTANCE)).inflate(1D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter, start, to, searchBox,
                (target) -> !target.isSpectator() && target.isPickable(), distSqr);

        if (entityHitResult != null) {
            pick = entityHitResult;
        }
        return pick;
    }

    public static void applyFreezeDamage(Entity entity, int base) {
        if(entity.canFreeze()){
            entity.hurt(DamageSource.FREEZE, entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES) ? 5 * base : base);
        }
    }

    public static void castTelekinesis(LivingEntity shooter) {
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            HitResult hitResult = getHitResult(shooter);
            if(hitResult instanceof BlockHitResult blockHitResult){
                fireBeamRaw(shooter, cap);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = shooter.level.getBlockState(blockPos);
                shooter.level.removeBlock(blockPos, false);
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(shooter.level, blockPos, blockState);
                fallingBlock.setNoGravity(true);
                cap.setTelekinesisTarget(fallingBlock);
            }
        });
    }

    public static void deactivateHeldPowers(Entity shooter) {
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            cap.setFiringBeam(false);
            shooter.level.getEntitiesOfClass(EnergyBeam.class, shooter.getBoundingBox().inflate(1D),
                            energyBeam -> energyBeam.getOwner() == shooter)
                    .forEach(Entity::discard);
            cap.getTelekinesisTarget(shooter.level).ifPresent(target -> target.setNoGravity(false));
            cap.setTelekinesisTarget(null);
        });
    }

    public static void tickActivePowers(Entity shooter) {
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            if (cap.isFiringBeam()) {
                onBeamTick(shooter, cap.getSuperpower(), cap.getTicksFiringBeam());
                cap.setTicksFiringBeam(cap.getTicksFiringBeam() + 1);
            } else{
                cap.setTicksFiringBeam(0);
            }
        });
    }

    public static void onBeamTick(Entity shooter, Superpower superpower, int ticksFiringBeam) {
        HitResult hitResult = getHitResult(shooter);
        Either<EntityHitResult, BlockHitResult> hitResultEither;
        if(hitResult instanceof EntityHitResult entityHitResult) hitResultEither = Either.left(entityHitResult);
        else hitResultEither = Either.right((BlockHitResult) hitResult);
        superpower.tickUse(shooter, hitResultEither, ticksFiringBeam);
    }

    public static void tickFire(Entity shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        if(ticksFiringBeam % 10 == 0){
            hitResultEither.mapBoth(entityHitResult -> {
                Entity entity = entityHitResult.getEntity();
                entity.setSecondsOnFire(5);
                return true;
            }, blockHitResult -> {
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = shooter.level.getBlockState(blockPos);
                if(!blockState.isAir() && !blockState.getMaterial().isLiquid()){
                    shooter.level.gameEvent(shooter, GameEvent.BLOCK_PLACE, blockPos);
                    shooter.level.setBlockAndUpdate(blockPos, Blocks.LAVA.defaultBlockState());
                }
                return true;
            });
        }
    }

    public static void tickLightning(Entity shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        if(ticksFiringBeam % 10 == 0){
            hitResultEither.ifLeft(entityHitResult -> {
                Entity entity = entityHitResult.getEntity();
                entity.hurt(DamageSource.LIGHTNING_BOLT, 4.0F);
            });
        }
    }

    public static void tickIce(Entity shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        if(ticksFiringBeam % 10 == 0){
            hitResultEither.ifLeft(entityHitResult -> {
                Entity entity = entityHitResult.getEntity();
                entity.setTicksFrozen(600); // 15 seconds * 2 since it decays at 2 tps
            });
        }
    }

    public static void tickTelekinesis(Entity shooter, Either<EntityHitResult, BlockHitResult> ignoredHitResultEither, int ignoredTicksFiringBeam){
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            cap.getTelekinesisTarget(shooter.level).ifPresent(target -> onTelekinesisTick(shooter, target));
        });
    }

    public static void onTelekinesisTick(Entity shooter, Entity target) {
        HitResult hitResult = getHitResult(shooter);
        Vec3 position = hitResult.getLocation();
        double xDist = position.x - target.getX();
        double yDist = position.y - target.getY();
        double zDist = position.z - target.getZ();
        double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
        if (dist != 0.0D) {
            double xPower = xDist / dist * 0.1D;
            double yPower = yDist / dist * 0.1D;
            double zPower = zDist / dist * 0.1D;
            target.push(xPower, yPower, zPower);
        }
    }
}
