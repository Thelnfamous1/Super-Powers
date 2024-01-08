package me.Thelnfamous1.super_powers.client;

import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.client.keymapping.SPKeymapping;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.network.C2SUpdateSuperpowerPacket;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import me.Thelnfamous1.super_powers.common.particle.ElectricShockParticle;
import me.Thelnfamous1.super_powers.common.particle.SnowflakeParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;

public class ClientHandler {
    private static final Map<Superpower, Boolean> SUPERPOWER_TOGGLES = new HashMap<>();

    public static void registerEventHandlers(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((RegisterKeyMappingsEvent event) ->
                SPKeymapping.SUPERPOWERS_BY_KEY.keySet().forEach(event::register));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(SuperPowers.ENERGY_BEAM.get(), EnergyBeamRenderer::new);
            event.registerEntityRenderer(SuperPowers.TELEKINESIS_BLOCK.get(), FallingBlockRenderer::new);
        });
        modEventBus.addListener((RegisterParticleProvidersEvent event) -> {
            event.register(SuperPowers.ELECTRIC_SHOCK_PARTICLE.get(), ElectricShockParticle.Factory::new);
            event.register(SuperPowers.SNOWFLAKE.get(), SnowflakeParticle.Factory::new);
        });
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            SPNetwork.SYNC_CHANNEL.sendToServer(new C2SUpdateSuperpowerPacket(C2SUpdateSuperpowerPacket.Action.SYNC));
        });
    }

    public static void tickInput(LocalPlayer player){
        SuperpowerCapability.getOptional(player).ifPresent(cap -> {
            for(Superpower superpower : cap.getSuperpowers()){
                boolean wasUsingSuperpower = SUPERPOWER_TOGGLES.getOrDefault(superpower, false);
                SUPERPOWER_TOGGLES.put(superpower, SPKeymapping.SUPERPOWERS_BY_KEY.inverse().get(superpower).isDown());
                boolean isUsingSuperpower = SUPERPOWER_TOGGLES.get(superpower);
                if (wasUsingSuperpower && !isUsingSuperpower) {
                    // Release
                    //SPNetwork.SYNC_CHANNEL.sendToServer(new C2SUpdateSuperpowerPacket(C2SUpdateSuperpowerPacket.Action.DEACTIVATE, superpower));
                } else if (!wasUsingSuperpower && isUsingSuperpower) {
                    // First press
                    SPNetwork.SYNC_CHANNEL.sendToServer(new C2SUpdateSuperpowerPacket(C2SUpdateSuperpowerPacket.Action.ACTIVATE, superpower));
                } else if (wasUsingSuperpower) {
                    // Held
                }
            }
        });

    }
}
