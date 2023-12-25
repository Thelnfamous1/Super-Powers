package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.nbt.CompoundTag;

public class SuperpowerCapabilityImplementation implements SuperpowerCapabilityInterface {
    public static final String TAG_KEY = "Superpower";
    private Superpower superpower = Superpower.NONE;

    @Override
    public Superpower getSuperpower() {
        return this.superpower;
    }

    @Override
    public void setSuperpower(Superpower superpower) {
        this.superpower = superpower;
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
