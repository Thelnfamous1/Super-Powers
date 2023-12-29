package me.Thelnfamous1.super_powers.common.entity;

import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.mixin.FallingBlockEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class TelekinesisBlockEntity extends FallingBlockEntity implements OwnableEntity {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(TelekinesisBlockEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> DATA_RELEASED = SynchedEntityData.defineId(TelekinesisBlockEntity.class, EntityDataSerializers.BOOLEAN);

    public TelekinesisBlockEntity(EntityType<? extends TelekinesisBlockEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private TelekinesisBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        this(SuperPowers.TELEKINESIS_BLOCK.get(), pLevel);
        ((FallingBlockEntityAccess)this).super_powers$setBlockState(pState);
        this.blocksBuilding = true;
        this.setPos(pX, pY, pZ);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = pX;
        this.yo = pY;
        this.zo = pZ;
        this.setStartPos(this.blockPosition());
    }

    public static TelekinesisBlockEntity telekinesis(Level pLevel, BlockPos pPos, BlockState pBlockState, LivingEntity owner) {
        TelekinesisBlockEntity telekinesisBlockEntity = new TelekinesisBlockEntity(pLevel,
                (double)pPos.getX() + 0.5D,
                pPos.getY(),
                (double)pPos.getZ() + 0.5D,
                pBlockState.hasProperty(BlockStateProperties.WATERLOGGED) ? pBlockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE) : pBlockState);
        pLevel.setBlock(pPos, pBlockState.getFluidState().createLegacyBlock(), 3);
        telekinesisBlockEntity.setOwner(owner);
        pLevel.addFreshEntity(telekinesisBlockEntity);
        return telekinesisBlockEntity;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_RELEASED, false);
    }

    @Override
    public void tick() {
        if(!this.isReleased()){
            if (this.getBlockState().isAir()) {
                this.discard();
            } else {
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
                if(!this.level.isClientSide){
                    if(this.getOwner() == null){
                        this.setReleased(true);
                    }
                }
            }
        } else{
            super.tick();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID != null){
            pCompound.putUUID("Owner", ownerUUID);
        }
        pCompound.putBoolean("Released", this.isReleased());

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if(pCompound.hasUUID("Owner")){
            this.setOwnerUUID(pCompound.getUUID("Owner"));
        }
        if(pCompound.contains("Released", Tag.TAG_ANY_NUMERIC)){
            this.setReleased(pCompound.getBoolean("Released"));
        }
    }

    public void setReleased(boolean released){
        this.entityData.set(DATA_RELEASED, released);
    }

    public boolean isReleased(){
        return this.entityData.get(DATA_RELEASED);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner){
        this.setOwnerUUID(owner == null ? null : owner.getUUID());
    }

    private void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID != null){
            return this.level.getPlayerByUUID(ownerUUID);
        }
        return null;
    }
}
