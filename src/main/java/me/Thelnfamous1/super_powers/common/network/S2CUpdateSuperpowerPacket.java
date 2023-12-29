package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class S2CUpdateSuperpowerPacket {
    private final int id;

    private final CompoundTag data;
    private final @Nullable Superpower activeSuperpower;
    private final int targetId;

    public S2CUpdateSuperpowerPacket(Entity entity, SuperpowerCapabilityInterface cap){
        this.id = entity.getId();
        this.data = cap.serializeNBT();
        this.activeSuperpower = cap.getActiveSuperpower().orElse(null);
        this.targetId = cap.getSuperpowerTarget(entity.level).map(Entity::getId).orElse(0);
    }

    public S2CUpdateSuperpowerPacket(FriendlyByteBuf buf){
        this.id = buf.readInt();
        this.data = buf.readNbt();
        this.activeSuperpower = buf.readOptional(FriendlyByteBuf::readByte).map(Superpower::byId).orElse(null);
        this.targetId = buf.readInt();
    }

    public void write(FriendlyByteBuf buf){
        buf.writeInt(this.id);
        buf.writeNbt(this.data);
        buf.writeOptional(Optional.ofNullable(this.activeSuperpower), (b, s) -> b.writeByte(s.getId()));
        buf.writeInt(this.targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.id);
            if(entity != null){
                SuperpowerCapability.getOptional(entity).ifPresent(cap -> {
                    cap.deserializeNBT(this.data);
                    cap.setActiveSuperpower(this.activeSuperpower);
                    cap.setSuperpowerTarget(Minecraft.getInstance().level.getEntity(this.targetId));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
