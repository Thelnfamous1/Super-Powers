package me.Thelnfamous1.super_powers.common.capability;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperpowerCapabilityAttacher {

    private static class SuperpowerCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        public static final ResourceLocation IDENTIFIER = new ResourceLocation(SuperPowers.MODID, "superpower");

        private final SuperpowerCapabilityInterface backend = new SuperpowerCapabilityImplementation();
        private final LazyOptional<SuperpowerCapabilityInterface> optionalData = LazyOptional.of(() -> backend);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return SuperpowerCapability.INSTANCE.orEmpty(cap, this.optionalData);
        }

        void invalidate() {
            this.optionalData.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }
    }

    public static void attach(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player){
            final SuperpowerCapabilityProvider provider = new SuperpowerCapabilityProvider();
            event.addCapability(SuperpowerCapabilityProvider.IDENTIFIER, provider);
        }
    }

    private SuperpowerCapabilityAttacher() {
    }
}