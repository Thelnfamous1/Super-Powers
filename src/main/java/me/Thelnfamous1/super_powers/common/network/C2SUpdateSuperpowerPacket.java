package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class C2SUpdateSuperpowerPacket {

    private final Action action;
    private final @Nullable Superpower superpower;

    public C2SUpdateSuperpowerPacket(Action action){
        this(action, null);
    }

    public C2SUpdateSuperpowerPacket(Action action, @Nullable Superpower superpower){
        this.action = action;
        this.superpower = superpower;
    }

    public C2SUpdateSuperpowerPacket(FriendlyByteBuf buf){
        this.action = buf.readEnum(Action.class);
        this.superpower = buf.readOptional(FriendlyByteBuf::readByte).map(Superpower::byId).orElse(null);
    }

    public void write(FriendlyByteBuf buf){
        buf.writeEnum(this.action);
        buf.writeOptional(Optional.ofNullable(this.superpower), (b, s) -> b.writeByte(s.getId()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null){
                switch (this.action){
                    case ACTIVATE -> SuperpowerCapability.getOptional(player).ifPresent(cap -> {
                        Superpower previousPower = null;
                        if (cap.isSuperpowerActive()) { // ensure not using multiple powers at once
                            previousPower = this.deactivateSuperpower(player, cap);
                        }
                        if(this.superpower != null && cap.hasSuperpower(this.superpower)){
                            if(this.superpower != previousPower){ // ensure not re-activating previous power
                                if(this.superpower.activate(player)) cap.setActiveSuperpower(this.superpower);
                            }
                        }
                        SPNetwork.sendSyncPacket(player, cap);
                    });
                    case DEACTIVATE -> SuperpowerCapability.getOptional(player).ifPresent(cap -> {
                        if(this.superpower != null && cap.hasSuperpower(this.superpower)){
                            if (cap.isActiveSuperpower(this.superpower)) {
                                this.deactivateSuperpower(player, cap);
                            }
                        }
                        SPNetwork.sendSyncPacket(player, cap);
                    });
                    case SYNC -> SuperpowerCapability.getOptional(player).ifPresent(cap -> SPNetwork.sendSyncPacket(player, cap));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private Superpower deactivateSuperpower(Player player, SuperpowerCapabilityInterface cap){
        return cap.getActiveSuperpower().map(superpower -> {
            cap.setActiveSuperpower(null);
            superpower.deactivate(player);
            return superpower;
        }).orElse(null);
    }

    public enum Action{
        ACTIVATE,
        DEACTIVATE,
        SYNC
    }
}
