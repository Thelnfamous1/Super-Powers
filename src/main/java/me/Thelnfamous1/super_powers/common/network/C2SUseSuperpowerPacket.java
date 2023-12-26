package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SUseSuperpowerPacket {

    private final Action action;

    public C2SUseSuperpowerPacket(Action action){
        this.action = action;
    }

    public C2SUseSuperpowerPacket(FriendlyByteBuf buf){
        this.action = buf.readEnum(Action.class);
    }

    public void write(FriendlyByteBuf buf){
        buf.writeEnum(this.action);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null){
                switch (this.action){
                    case START -> SuperpowerCapability.getOptional(player).ifPresent(cap -> {
                        Superpower superpower = cap.getSuperpower();
                        if(!superpower.isNone()){
                            if (!player.isSecondaryUseActive()) {
                                superpower.activatePrimary(player);
                            } else {
                                superpower.activateSecondary(player);
                            }
                        }
                        SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CSetSuperpowerPacket(player, cap));
                    });
                    case STOP -> SuperpowerCapability.getOptional(player).ifPresent(cap -> {
                        SuperpowerHelper.deactivateHeldPowers(player);
                        SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CSetSuperpowerPacket(player, cap));
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Action{
        START,
        STOP
    }
}
