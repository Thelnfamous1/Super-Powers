package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface SuperpowerCapabilityInterface extends INBTSerializable<CompoundTag> {

    Superpower getSuperpower();

    void setSuperpower(Superpower superpower);

    boolean isFiringBeam();

    void setFiringBeam(boolean firingBeam);

    int getTicksFiringBeam();

    void setTicksFiringBeam(int ticksFiringBeam);
}
