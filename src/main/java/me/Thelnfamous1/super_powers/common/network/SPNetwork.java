package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

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
        SYNC_CHANNEL.registerMessage(INDEX++, C2SUseSuperpowerPacket.class, C2SUseSuperpowerPacket::write, C2SUseSuperpowerPacket::new, C2SUseSuperpowerPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        SYNC_CHANNEL.registerMessage(INDEX++, S2CSetSuperpowerPacket.class, S2CSetSuperpowerPacket::write, S2CSetSuperpowerPacket::new, S2CSetSuperpowerPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        SYNC_CHANNEL.registerMessage(INDEX++, C2SEnergyBeamPacket.class, C2SEnergyBeamPacket::write, C2SEnergyBeamPacket::new, C2SEnergyBeamPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
