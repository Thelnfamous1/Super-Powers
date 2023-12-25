package me.Thelnfamous1.super_powers.common.entity;

import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import me.Thelnfamous1.super_powers.common.network.C2SEnergyBeamPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnergyBeam extends Entity implements IEntityAdditionalSpawnData, OwnableEntity {
    private static final EntityDataAccessor<Float> BEAM_WIDTH = SynchedEntityData.defineId(EnergyBeam.class, EntityDataSerializers.FLOAT);
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public EnergyBeam(EntityType<?> type, Level world) {
        super(type, world);
    }

    public EnergyBeam(Level world, LivingEntity shooter) {
        this(SuperPowers.ENERGY_BEAM.get(), world, shooter);
    }

    public EnergyBeam(EntityType<?> type, Level world, LivingEntity shooter) {
        super(type, world);
        this.setOwner(shooter);
        Vec3 offset = offsetInFrontOfEntity(shooter);
        this.moveTo(offset.x, offset.y, offset.z, shooter.getYRot(), shooter.getXRot());
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        if(owner != null){
            this.ownerUUID = owner.getUUID();
            this.updatePositionAndRotation(this.owner);
        }
    }

    @Override
    public void tick() {
        LivingEntity owner = this.getOwner();
        if(!this.level.isClientSide) {
            if (owner == null || !owner.isAlive()) {
                this.discard();
                return;
            }
        }
        if (this.owner instanceof Player && this.level.isClientSide()){
            this.updatePositionAndRotation(this.owner);
            SPNetwork.SYNC_CHANNEL.sendToServer(new C2SEnergyBeamPacket(this));
        } else if (this.owner != null && !(this.owner instanceof Player) && !this.level.isClientSide()) {
            this.updatePositionAndRotation(this.owner);
        }
    }

    public void updatePositionAndRotation(LivingEntity owner) {
        Vec3 offset = offsetInFrontOfEntity(owner);
        this.setPos(offset.x, offset.y, offset.z);
        this.setYRot(this.boundDegrees(owner.getYRot()));
        this.setXRot(this.boundDegrees(owner.getXRot()));
        this.yRotO = this.boundDegrees(owner.yRotO);
        this.xRotO = this.boundDegrees(owner.xRotO);
    }

    private static Vec3 offsetInFrontOfEntity(LivingEntity entity) {
        return new Vec3(entity.getX(), entity.getY(0.5), entity.getZ())
                .add(entity.getLookAngle().scale(1.0D));
    }

    private float boundDegrees(float degrees){
        return (degrees % 360 + 360) % 360;
    }

    public float getBeamWidth() {
        return this.entityData.get(BEAM_WIDTH);
    }

    public void setBeamWidth(float beamWidth){
        this.entityData.set(BEAM_WIDTH, beamWidth);
    }

    public final Vec3 getWorldPosition(float partialTicks) {
        double x = Mth.lerp(partialTicks, this.xo, this.getX());
        double y = Mth.lerp(partialTicks, this.yo, this.getY());
        double z = Mth.lerp(partialTicks, this.zo, this.getZ());
        return new Vec3(x, y, z);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Nullable
    public LivingEntity getOwner() {
        if(this.owner == null && this.ownerUUID != null){
            if(this.level instanceof ServerLevel) {
                Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUUID);
                if (entity instanceof LivingEntity) {
                    this.owner = (LivingEntity) entity;
                }
            } else if(this.level.isClientSide) {
                this.owner = this.level.getPlayerByUUID(this.ownerUUID);
            }
        }
        return this.owner;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(BEAM_WIDTH, 0.5F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNullable(this.ownerUUID, FriendlyByteBuf::writeUUID);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.ownerUUID = additionalData.readNullable(FriendlyByteBuf::readUUID);
    }
}