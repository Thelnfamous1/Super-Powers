package me.Thelnfamous1.super_powers.common.network;

import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SEnergyBeamPacket {

    private final int energyBeamId;
    private final double positionX;
    private final double positionY;
    private final double positionZ;
    private final float xRot;
    private final float yRot;
    private final float xRotO;
    private final float yRotO;

    public C2SEnergyBeamPacket(EnergyBeam energyBeam) {
        this.energyBeamId = energyBeam.getId();
        this.positionX = energyBeam.position().x;
        this.positionY = energyBeam.position().y;
        this.positionZ = energyBeam.position().z;
        this.xRot = energyBeam.getXRot();
        this.yRot = energyBeam.getYRot();
        this.xRotO = energyBeam.xRotO;
        this.yRotO = energyBeam.yRotO;
    }

    public C2SEnergyBeamPacket(int energyBeamId, double positionX, double positionY, double positionZ, float xRot, float yRot, float xRotO, float yRotO) {
        this.energyBeamId = energyBeamId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.xRot = xRot;
        this.yRot = yRot;
        this.xRotO = xRotO;
        this.yRotO = yRotO;
    }

    public C2SEnergyBeamPacket(FriendlyByteBuf buf) {
        this(
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.energyBeamId);
        buf.writeDouble(this.positionX);
        buf.writeDouble(this.positionY);
        buf.writeDouble(this.positionZ);
        buf.writeFloat(this.xRot);
        buf.writeFloat(this.yRot);
        buf.writeFloat(this.xRotO);
        buf.writeFloat(this.yRotO);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level.getEntity(this.energyBeamId);
                if(entity instanceof EnergyBeam energyBeam) {
                    if(energyBeam.getOwner() != player) return;
                    energyBeam.setPos(this.positionX, this.positionY, this.positionZ);
                    energyBeam.setXRot(this.xRot);
                    energyBeam.setYRot(this.yRot);
                    energyBeam.xRotO = this.xRotO;
                    energyBeam.yRotO = this.yRotO;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}