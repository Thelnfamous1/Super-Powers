package me.Thelnfamous1.super_powers.client;

import me.Thelnfamous1.super_powers.client.keymapping.SPKeymapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientHandler {

    public static void registerEventHandlers(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(SPKeymapping.USE_ABILITY));
    }
}
