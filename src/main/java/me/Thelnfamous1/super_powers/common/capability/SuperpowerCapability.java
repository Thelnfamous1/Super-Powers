package me.Thelnfamous1.super_powers.common.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SuperpowerCapability {

    public static final Capability<SuperpowerCapabilityInterface> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static SuperpowerCapabilityInterface getCapability(ICapabilityProvider entity){
        return entity.getCapability(INSTANCE).orElseThrow(() -> new IllegalStateException("Missing Superpower Capability!"));
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(SuperpowerCapabilityInterface.class);
    }

    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        SuperpowerCapabilityInterface oldCap = getCapability(original);
        SuperpowerCapabilityInterface newCap = getCapability(event.getEntity());
        newCap.deserializeNBT(oldCap.serializeNBT()); // copies the data from old to new
        original.invalidateCaps();
    }

    private SuperpowerCapability() {
    }
}
