package me.Thelnfamous1.super_powers.common.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SuperpowerCapability {

    public static final Capability<SuperpowerCapabilityInterface> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static SuperpowerCapabilityInterface getCapability(ICapabilityProvider entity){
        return getOptional(entity).orElseThrow(() -> new IllegalStateException("Missing Superpower Capability!"));
    }

    public static LazyOptional<SuperpowerCapabilityInterface> getOptional(ICapabilityProvider entity){
        return entity.getCapability(INSTANCE);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(SuperpowerCapabilityInterface.class);
    }

    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        getOptional(original)
                .ifPresent(oldCap -> getOptional(event.getEntity())
                        .ifPresent(newCap -> newCap.deserializeNBT(oldCap.serializeNBT())));
        original.invalidateCaps();
    }

    private SuperpowerCapability() {
    }
}
