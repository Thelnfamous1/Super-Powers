package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SuperpowerCapabilityImplementation implements SuperpowerCapabilityInterface {
    public static final String TAG_KEY = "Superpower";
    private Superpower superpower = Superpower.NONE;
    private boolean firingBeam;
    private int ticksFiringBeam;
    @Nullable
    private Entity telekinesisTarget;
    @Nullable
    private UUID telekinesisTargetUUID;
    private int telekinesisTargetId;

    @Override
    public Superpower getSuperpower() {
        return this.superpower;
    }

    @Override
    public void setSuperpower(Superpower superpower) {
        this.superpower = superpower;
    }

    @Override
    public boolean isFiringBeam() {
        return this.firingBeam;
    }

    @Override
    public void setFiringBeam(boolean firingBeam) {
        this.firingBeam = firingBeam;
        this.setTicksFiringBeam(0);
    }

    @Override
    public int getTicksFiringBeam() {
        return this.ticksFiringBeam;
    }

    @Override
    public void setTicksFiringBeam(int ticksFiringBeam) {
        this.ticksFiringBeam = ticksFiringBeam;
    }

    @Override
    public Optional<Entity> getTelekinesisTarget(Level level) {
        if(this.telekinesisTarget == null){
            if(this.telekinesisTargetUUID != null && level instanceof ServerLevel serverLevel){
                this.telekinesisTarget = serverLevel.getEntity(this.telekinesisTargetUUID);
            } else if(this.telekinesisTargetId > 0){
                this.telekinesisTarget = level.getEntity(this.telekinesisTargetId);
            }
        }
        return Optional.ofNullable(this.telekinesisTarget);
    }

    @Override
    public void setTelekinesisTarget(@Nullable Entity telekinesisTarget) {
        this.telekinesisTarget = telekinesisTarget;
        this.telekinesisTargetUUID = telekinesisTarget != null ? telekinesisTarget.getUUID() : null;
        this.telekinesisTargetId = telekinesisTarget != null ? telekinesisTarget.getId() : 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putByte(TAG_KEY, (byte) this.superpower.getId());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.superpower = Superpower.byId(nbt.getByte(TAG_KEY));
    }
}
