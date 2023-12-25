package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SPNetwork {
    public static final String PROTOCOL = "1.0";
    public static final SimpleChannel SYNC_CHANNEL = NetworkRegistry.newSimpleChannel(
            SuperPowers.location("sync_channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);
    private static int INDEX;

    public static void register(FMLCommonSetupEvent event){
        event.enqueueWork(SPNetwork::init);
    }

    private static void init(){
    }
}
