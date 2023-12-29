package me.Thelnfamous1.super_powers.common.util;

import com.mojang.datafixers.util.Either;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import me.Thelnfamous1.super_powers.common.entity.TelekinesisBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
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

import java.util.function.Predicate;

public class SuperpowerHelper {

    public static final String ICE_SUPERPOWER_TAG = "%s:ice";
    public static final int USE_BONEMEAL_EVENT_ID = 1505;
    public static final int SHOOT_FIREBALL_EVENT_ID = 1018;
    public static final double MAX_HIT_DISTANCE = 1024D;
    public static final int TELEKINETIC_HOLD_DISTANCE = 3;

    public static boolean activateLightning(Player player) {
        if(player.isSecondaryUseActive()){
            return fireBeam(player);
        }
        HitResult hitResult = getHitResult(player);
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(player.level);
        if(lightningBolt != null){
            Vec3 target = hitResult.getLocation();
            lightningBolt.moveTo(target.x, target.y, target.z, player.getYRot(), lightningBolt.getXRot()); // bolts can only be vertical, so use its default x rot
            if(player instanceof ServerPlayer player1) lightningBolt.setCause(player1);
            player.level.addFreshEntity(lightningBolt);
        }
        return false;
    }

    public static boolean fireBeam(LivingEntity shooter){
        return SuperpowerCapability.getOptional(shooter)
                .map(cap -> fireBeamRaw(shooter))
                .orElse(false);
    }

    private static boolean fireBeamRaw(LivingEntity shooter) {
        EnergyBeam energyBeam = new EnergyBeam(shooter.level, shooter);
        shooter.level.addFreshEntity(energyBeam);
        return true;
    }

    public static HitResult getHitResult(Entity shooter){
        return getHitResult(shooter, MAX_HIT_DISTANCE, SuperpowerHelper::canBeHit);
    }

    private static boolean canBeHit(Entity target) {
        return !target.isSpectator() && target.isPickable();
    }

    public static HitResult getHitResult(Entity shooter, double distance){
        return getHitResult(shooter, distance, SuperpowerHelper::canBeHit);
    }

    public static HitResult getHitResult(Entity shooter, double distance, Predicate<Entity> filter){
        Vec3 start = shooter.getEyePosition(1.0F);
        HitResult pick = shooter.pick(distance, 1.0F, false);
        double distSqr = Mth.square(distance);
        if (pick.getType() != HitResult.Type.MISS) {
            distSqr = pick.getLocation().distanceToSqr(start);
        }

        Vec3 viewVector = shooter.getViewVector(1.0F);
        Vec3 to = start.add(viewVector.scale(distance));
        AABB searchBox = shooter.getBoundingBox().expandTowards(viewVector.scale(distance)).inflate(1D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter, start, to, searchBox, filter, distSqr);

        if (entityHitResult != null) {
            pick = entityHitResult;
        }
        return pick;
    }

    public static boolean activateFire(Player player) {
        if(player.isSecondaryUseActive()){
            return fireBeam(player);
        }
        HitResult hitResult = getHitResult(player);
        Vec3 target = hitResult.getLocation();
        double xDist = target.x - player.getX();
        double yDist = target.y - player.getY();
        double zDist = target.z - player.getZ();
        SmallFireball fireball = new SmallFireball(player.level, player, xDist, yDist, zDist);
        fireball.setPos(fireball.getX(), player.getEyeY() - 0.1D, fireball.getZ()); // similar positioning to Snowball
        player.level.levelEvent(null, SHOOT_FIREBALL_EVENT_ID, player.blockPosition(), 0);
        player.level.addFreshEntity(fireball);
        return false;
    }

    public static boolean activateIce(Player player) {
        if(player.isSecondaryUseActive()){
            return fireBeam(player);
        }
        Snowball snowball = new Snowball(player.level, player);
        snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 0.0F);
        snowball.getTags().add(ICE_SUPERPOWER_TAG);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (player.level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.level.addFreshEntity(snowball);
        return false;
    }

    public static void applyFreezeDamage(Entity entity, int base) {
        if(entity.canFreeze()){
            entity.hurt(DamageSource.FREEZE, entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES) ? 5 * base : base);
        }
    }

    public static boolean activateEarth(LivingEntity shooter){
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
        return false;
    }

    public static boolean activateTelekinesis(LivingEntity shooter) {
        return SuperpowerCapability.getOptional(shooter).map(cap -> {
            HitResult hitResult = getHitResult(shooter);
            if(hitResult.getType() != HitResult.Type.MISS && hitResult instanceof BlockHitResult blockHitResult){
                fireBeamRaw(shooter);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = shooter.level.getBlockState(blockPos);
                TelekinesisBlockEntity telekinesisBlockEntity = TelekinesisBlockEntity.telekinesis(shooter.level, blockPos, blockState, shooter);
                cap.setSuperpowerTarget(telekinesisBlockEntity);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public static void tickActivePowers(Player shooter) {
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            if (cap.isSuperpowerActive()) {
                cap.getActiveSuperpower().ifPresent(superpower -> onActiveSuperpower(shooter, superpower, cap.getActiveSuperpowerTicks()));
                cap.setActiveSuperpowerTicks(cap.getActiveSuperpowerTicks() + 1);
            } else{
                cap.setActiveSuperpowerTicks(0);
            }
        });
    }

    private static void onActiveSuperpower(Player shooter, Superpower superpower, int ticksFiringBeam) {
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
                if(blockHitResult.getType() != HitResult.Type.MISS && !shooter.level.isClientSide){
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = shooter.level.getBlockState(blockPos);
                    if(!blockState.isAir() && !blockState.getMaterial().isLiquid()){
                        shooter.level.gameEvent(shooter, GameEvent.BLOCK_PLACE, blockPos);
                        shooter.level.setBlockAndUpdate(blockPos, Blocks.LAVA.defaultBlockState());
                    }
                }
                return true;
            });
        }
    }

    public static void tickLightning(LivingEntity shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        if(ticksFiringBeam % 10 == 0){
            hitResultEither.mapBoth(entityHitResult -> {
                Entity entity = entityHitResult.getEntity();
                entity.hurt(DamageSource.LIGHTNING_BOLT, 4.0F);
                return true;
            }, blockHitResult -> {
                if(blockHitResult.getType() != HitResult.Type.MISS && !shooter.level.isClientSide){
                    Vec3 position = blockHitResult.getLocation();
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = shooter.level.getBlockState(blockPos);
                    if(!blockState.isAir() && !blockState.getMaterial().isLiquid()){
                        BlockPos above = blockPos.above();
                        BlockState aboveState = shooter.level.getBlockState(above);
                        if(aboveState.isAir() || aboveState.getMaterial().isReplaceable()){
                            if(shooter.level.getEntitiesOfClass(AreaEffectCloud.class, AABB.ofSize(position, 1, 1, 1)).isEmpty()){
                                AreaEffectCloud areaEffectCloud = new AreaEffectCloud(shooter.level, above.getX() + 0.5, above.getY(), above.getZ() + 0.5);
                                areaEffectCloud.setRadius(0.5F);
                                areaEffectCloud.setDuration(200);
                                areaEffectCloud.addEffect(new MobEffectInstance(SuperPowers.ELECTRIC_SHOCK_EFFECT.get(), 1));
                                areaEffectCloud.setParticle(SuperPowers.ELECTRIC_SHOCK_PARTICLE.get());
                                areaEffectCloud.setOwner(shooter);
                                shooter.level.addFreshEntity(areaEffectCloud);
                            }
                        }
                    }
                }
                return true;
            });
        }
    }

    public static void tickIce(Entity ignoredShooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        if(ticksFiringBeam % 10 == 0){
            hitResultEither.ifLeft(entityHitResult -> {
                Entity entity = entityHitResult.getEntity();
                entity.setTicksFrozen(600); // 15 seconds * 2 since it decays at 2 tps
            });
        }
    }

    public static void tickEarth(Entity ignoredShooter, Either<EntityHitResult, BlockHitResult> ignoredHitResultEither, int ignoredTicksFiringBeam){
    }

    public static void tickTelekinesis(Entity shooter, Either<EntityHitResult, BlockHitResult> ignoredHitResultEither, int ignoredTicksFiringBeam){
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            cap.getSuperpowerTarget(shooter.level).ifPresent(target -> onTelekinesisTick(shooter, target));
        });
    }

    private static void onTelekinesisTick(Entity shooter, Entity target) {
        HitResult hitResult = getHitResult(shooter, TELEKINETIC_HOLD_DISTANCE, (entity) -> canBeHit(entity) && entity != target);
        Vec3 position = hitResult.getLocation();
        double xDist = position.x - target.getX();
        double yDist = position.y - target.getY();
        double zDist = position.z - target.getZ();
        double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
        if (dist != 0.0D) {
            double xPower = xDist / dist * 0.1D;
            double yPower = yDist / dist * 0.1D;
            double zPower = zDist / dist * 0.1D;
            target.setDeltaMovement(target.getDeltaMovement().add(xPower, yPower, zPower).scale(0.95D));
        }
    }

    public static void deactivateLightning(Player shooter){
        deactivateBeam(shooter);
    }

    private static void deactivateBeam(Player shooter){
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            shooter.level.getEntitiesOfClass(EnergyBeam.class, shooter.getBoundingBox().inflate(1D),
                            energyBeam -> energyBeam.getOwner() == shooter)
                    .forEach(Entity::discard);
        });
    }

    public static void deactivateFire(Player shooter){
        deactivateBeam(shooter);
    }

    public static void deactivateIce(Player shooter){
        deactivateBeam(shooter);
    }

    public static void deactivateEarth(Entity ignoredShooter){
    }

    public static void deactivateTelekinesis(Player shooter) {
        SuperpowerCapability.getOptional(shooter).ifPresent(cap -> {
            deactivateBeam(shooter);
            cap.getSuperpowerTarget(shooter.level).ifPresent(target -> {
                if(target instanceof TelekinesisBlockEntity tbe) tbe.setReleased(true);
                if(shooter.isSecondaryUseActive()){
                    shootFromRotation(target, shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 3.0F, 0.0F);
                    target.hurtMarked = true;
                }
            });
            cap.setSuperpowerTarget(null);
        });
    }

    private static void shootFromRotation(Entity projectile, Entity pShooter, float pX, float pY, float pZ, float pVelocity, float pInaccuracy) {
        float f = -Mth.sin(pY * Mth.DEG_TO_RAD) * Mth.cos(pX * Mth.DEG_TO_RAD);
        float f1 = -Mth.sin((pX + pZ) * Mth.DEG_TO_RAD);
        float f2 = Mth.cos(pY * Mth.DEG_TO_RAD) * Mth.cos(pX * Mth.DEG_TO_RAD);
        shoot(projectile, f, f1, f2, pVelocity, pInaccuracy);
        Vec3 shooterDeltaMove = pShooter.getDeltaMovement();
        projectile.setDeltaMovement(projectile.getDeltaMovement().add(shooterDeltaMove.x, pShooter.isOnGround() ? 0.0D : shooterDeltaMove.y, shooterDeltaMove.z));
        projectile.hurtMarked =true;
    }

    private static void shoot(Entity projectile, double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        Vec3 deltaMove = (new Vec3(pX, pY, pZ)).normalize().add(
                projectile.level.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy),
                projectile.level.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy),
                projectile.level.random.triangle(0.0D, 0.0172275D * (double)pInaccuracy)).scale(pVelocity);
        projectile.setDeltaMovement(deltaMove);
        double horizontalDistance = deltaMove.horizontalDistance();
        projectile.setYRot((float)(Mth.atan2(deltaMove.x, deltaMove.z) * (double)Mth.RAD_TO_DEG));
        projectile.setXRot((float)(Mth.atan2(deltaMove.y, horizontalDistance) * (double)Mth.RAD_TO_DEG));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }
}
