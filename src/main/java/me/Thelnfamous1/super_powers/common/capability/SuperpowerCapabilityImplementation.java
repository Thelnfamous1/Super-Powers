package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SuperpowerCapabilityImplementation implements SuperpowerCapabilityInterface {
    public static final String TAG_KEY = "Superpowers";
    private Set<Superpower> superpowers = new HashSet<>();
    private boolean firingBeam;
    private int activeSuperpowerTicks;
    @Nullable
    private Entity superpowerTarget;
    @Nullable
    private UUID superpowerTargetUUID;
    private int superpowerTargetId;
    @Nullable
    private Superpower activeSuperpower;

    @Override
    public Collection<Superpower> getSuperpowers() {
        return this.superpowers;
    }

    @Override
    public boolean addSuperpower(Superpower superpower) {
        return this.superpowers.add(superpower);
    }

    @Override
    public boolean removeSuperpower(Superpower superpower) {
        return this.superpowers.remove(superpower);
    }

    @Override
    public int getActiveSuperpowerTicks() {
        return this.activeSuperpowerTicks;
    }

    @Override
    public void setActiveSuperpowerTicks(int activeSuperpowerTicks) {
        this.activeSuperpowerTicks = activeSuperpowerTicks;
    }

    @Override
    public Optional<Entity> getSuperpowerTarget(Level level) {
        if(this.superpowerTarget != null && this.superpowerTarget.isRemoved()){
            this.superpowerTarget = null;
        }
        if(this.superpowerTarget == null){
            if(this.superpowerTargetUUID != null && level instanceof ServerLevel serverLevel){
                this.superpowerTarget = serverLevel.getEntity(this.superpowerTargetUUID);
            } else if(this.superpowerTargetId > 0){
                this.superpowerTarget = level.getEntity(this.superpowerTargetId);
            }
        }
        return Optional.ofNullable(this.superpowerTarget);
    }

    @Override
    public void setSuperpowerTarget(@Nullable Entity superpowerTarget) {
        this.superpowerTarget = superpowerTarget;
        this.superpowerTargetUUID = superpowerTarget != null ? superpowerTarget.getUUID() : null;
        this.superpowerTargetId = superpowerTarget != null ? superpowerTarget.getId() : 0;
    }

    @Override
    public Optional<Superpower> getActiveSuperpower() {
        return Optional.ofNullable(this.activeSuperpower);
    }

    @Override
    public void setActiveSuperpower(@Nullable Superpower activeSuperpower) {
        this.activeSuperpower = activeSuperpower;
        this.setActiveSuperpowerTicks(0);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        List<Integer> superpowerIds = this.superpowers.stream().map(Superpower::getId).toList();
        tag.putIntArray(TAG_KEY, superpowerIds);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.superpowers = Arrays.stream(nbt.getIntArray(TAG_KEY)).mapToObj(Superpower::byId).collect(Collectors.toSet());
    }
}
