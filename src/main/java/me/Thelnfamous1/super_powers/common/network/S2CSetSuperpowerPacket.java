package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetSuperpowerPacket {
    private final int id;

    private final CompoundTag data;
    private final boolean firingBeam;
    private final int targetId;

    public S2CSetSuperpowerPacket(Entity entity, SuperpowerCapabilityInterface cap){
        this.id = entity.getId();
        this.data = cap.serializeNBT();
        this.firingBeam = cap.isFiringBeam();
        this.targetId = cap.getTelekinesisTarget(entity.level).map(Entity::getId).orElse(0);
    }

    public S2CSetSuperpowerPacket(FriendlyByteBuf buf){
        this.id = buf.readInt();
        this.data = buf.readNbt();
        this.firingBeam = buf.readBoolean();
        this.targetId = buf.readInt();
    }

    public void write(FriendlyByteBuf buf){
        buf.writeInt(this.id);
        buf.writeNbt(this.data);
        buf.writeBoolean(this.firingBeam);
        buf.writeInt(this.targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.id);
            if(entity != null){
                SuperpowerCapability.getOptional(entity).ifPresent(cap -> {
                    cap.deserializeNBT(this.data);
                    cap.setFiringBeam(this.firingBeam);
                    cap.setTelekinesisTarget(Minecraft.getInstance().level.getEntity(this.targetId));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
