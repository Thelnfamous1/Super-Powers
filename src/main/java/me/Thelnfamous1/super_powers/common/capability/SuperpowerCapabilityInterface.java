package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public interface SuperpowerCapabilityInterface extends INBTSerializable<CompoundTag> {

    Collection<Superpower> getSuperpowers();

    default boolean hasSuperpower(Superpower superpower){
        return this.getSuperpowers().contains(superpower);
    }

    boolean addSuperpower(Superpower superpower);

    boolean removeSuperpower(Superpower superpower);

    default boolean isSuperpowerActive(){
        return this.getActiveSuperpower().isPresent();
    }

    default boolean isActiveSuperpower(Superpower superpower){
        return this.getActiveSuperpower().map(active -> active.equals(superpower)).orElse(false);
    }

    int getActiveSuperpowerTicks();

    void setActiveSuperpowerTicks(int activeSuperpowerTicks);

    Optional<Entity> getSuperpowerTarget(Level level);

    void setSuperpowerTarget(@Nullable Entity superpowerTarget);

    Optional<Superpower> getActiveSuperpower();

    void setActiveSuperpower(@Nullable Superpower activeSuperpower);
}
