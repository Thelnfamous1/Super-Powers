package me.Thelnfamous1.super_powers.client;

import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.client.keymapping.SPKeymapping;
import me.Thelnfamous1.super_powers.common.network.C2SUseSuperpowerPacket;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientHandler {
    private static boolean isUsingSuperpower;

    public static void registerEventHandlers(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(SPKeymapping.USE_SUPERPOWER));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(SuperPowers.ENERGY_BEAM.get(), EnergyBeamRenderer::new));
    }

    public static void tickInput(LocalPlayer player){
        boolean wasUsingSuperpower = isUsingSuperpower;
        isUsingSuperpower = SPKeymapping.USE_SUPERPOWER.isDown();
        if (wasUsingSuperpower && !isUsingSuperpower) {
            SPNetwork.SYNC_CHANNEL.sendToServer(new C2SUseSuperpowerPacket(C2SUseSuperpowerPacket.Action.STOP));
        } else if (!wasUsingSuperpower && isUsingSuperpower) {
            SPNetwork.SYNC_CHANNEL.sendToServer(new C2SUseSuperpowerPacket(C2SUseSuperpowerPacket.Action.START));
            // start power use
        } else if (wasUsingSuperpower) {
        }

    }
}
