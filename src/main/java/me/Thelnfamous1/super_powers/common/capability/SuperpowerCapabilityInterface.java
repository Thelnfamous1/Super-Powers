package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface SuperpowerCapabilityInterface extends INBTSerializable<CompoundTag> {

    Superpower getSuperpower();

    void setSuperpower(Superpower superpower);

    boolean isFiringBeam();

    void setFiringBeam(boolean firingBeam);

    int getTicksFiringBeam();

    void setTicksFiringBeam(int ticksFiringBeam);

    Optional<Entity> getTelekinesisTarget(Level level);

    void setTelekinesisTarget(@Nullable Entity telekinesisTarget);
}
