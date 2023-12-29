package me.Thelnfamous1.super_powers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.HitResult;

public class EnergyBeamRenderer<T extends EnergyBeam> extends EntityRenderer<T> {

    private static final float SPEED_MODIFIER = -0.02F;

    public EnergyBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T energyBeam) {
        return SPRenderType.CORE_LOCATION;
    }

    @Override
    public void render(T energyBeam, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        Entity owner = energyBeam.getOwner();

        if(owner != null){
            SuperpowerCapability.getOptional(owner).ifPresent(cap -> {
                HitResult hitResult = SuperpowerHelper.getHitResult(owner, cap.getSuperpowerTarget(owner.level)
                        .map(target -> (double)target.distanceTo(owner))
                        .orElse(SuperpowerHelper.MAX_HIT_DISTANCE));
                double distanceTo = hitResult.distanceTo(owner);
                float[] beamColor = cap.getActiveSuperpower()
                        .map(Superpower::getDyeColor)
                        .orElse(DyeColor.WHITE)
                        .getTextureDiffuseColors();
                drawBeams(energyBeam, beamColor, pPoseStack, distanceTo, SPEED_MODIFIER, pPartialTicks);
            });
        }
    }

    private static void drawBeams(EnergyBeam energyBeam, float[] color, PoseStack pPoseStack, double distance, float speedModifier, float partialTicks) {
        VertexConsumer builder;
        long gameTime = energyBeam.level.getGameTime();
        double v = gameTime * speedModifier;
        float additiveThickness = (energyBeam.getBeamWidth() * 1.75F) * calculateLaserFlickerModifier(gameTime);

        float r = 1.0F; // already divided by 255
        float g = 1.0F; // already divided by 255
        float b = 1.0F; // already divided by 255
        float innerR = color[0]; // already divided by 255
        float innerG = color[1]; // already divided by 255
        float innerB = color[2]; // already divided by 255
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees((Mth.lerp(partialTicks, boundDegrees(-energyBeam.getYRot()), boundDegrees(-energyBeam.yRotO)))));
        pPoseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTicks, boundDegrees(energyBeam.getXRot()), boundDegrees(energyBeam.xRotO))));

        PoseStack.Pose last = pPoseStack.last();
        Matrix3f normal = last.normal();
        Matrix4f pose = last.pose();

        //additive laser beam
        builder = buffer.getBuffer(SPRenderType.ENERGY_BEAM_GLOW);
        drawClosingBeam(builder, pose, normal, additiveThickness, distance / 10.0D, 0.5D, 1, partialTicks,
                innerR, innerG, innerB,0.9F);

        //main laser, colored part
        builder = buffer.getBuffer(SPRenderType.ENERGY_BEAM_MAIN);
        drawBeam(builder, pose, normal, energyBeam.getBeamWidth(), distance, v, v + distance * 1.5D, partialTicks,
                innerR, innerG, innerB, 0.7F);

        //core
        builder = buffer.getBuffer(SPRenderType.ENERGY_BEAM_CORE);
        drawBeam(builder, pose, normal, energyBeam.getBeamWidth() * 0.7F, distance, v, v + distance * 1.5D, partialTicks,
                r, g, b, 1.0F);
        pPoseStack.popPose();
        buffer.endLastBatch();
    }

    private static float boundDegrees(float degrees){
        return (degrees % 360.0F + 360.0F) % 360.0F;
    }

    private static float calculateLaserFlickerModifier(long gameTime) {
        return 0.9F + 0.1F * Mth.sin(gameTime * 0.99F) * Mth.sin(gameTime * 0.3F) * Mth.sin(gameTime * 0.1F);
    }

    private static void drawBeam(VertexConsumer builder, Matrix4f pose, Matrix3f normal, float thickness, double distance, double v1, double v2, float ticks, float r, float g, float b, float alpha) {
        Vector3f vector3f = new Vector3f(0.0F, 1.0F, 0.0F);
        vector3f.transform(normal);
        float xMin = -thickness;
        float xMax = thickness;
        float yMin = -thickness - 0.115F;
        float yMax = thickness - 0.115F;
        float zMin = 0;
        float zMax = (float) distance;

        Vector4f vec1 = new Vector4f(xMin, yMin, zMin, 1.0F);
        vec1.transform(pose);
        Vector4f vec2 = new Vector4f(xMin, yMin, zMax, 1.0F);
        vec2.transform(pose);
        Vector4f vec3 = new Vector4f(xMin, yMax, zMax, 1.0F);
        vec3.transform(pose);
        Vector4f vec4 = new Vector4f(xMin, yMax, zMin, 1.0F);
        vec4.transform(pose);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMax, yMin, zMin, 1.0F);
        vec1.transform(pose);
        vec2 = new Vector4f(xMax, yMin, zMax, 1.0F);
        vec2.transform(pose);
        vec3 = new Vector4f(xMax, yMax, zMax, 1.0F);
        vec3.transform(pose);
        vec4 = new Vector4f(xMax, yMax, zMin, 1.0F);
        vec4.transform(pose);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMin, yMax, zMin, 1.0F);
        vec1.transform(pose);
        vec2 = new Vector4f(xMin, yMax, zMax, 1.0F);
        vec2.transform(pose);
        vec3 = new Vector4f(xMax, yMax, zMax, 1.0F);
        vec3.transform(pose);
        vec4 = new Vector4f(xMax, yMax, zMin, 1.0F);
        vec4.transform(pose);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMin, yMin, zMin, 1.0F);
        vec1.transform(pose);
        vec2 = new Vector4f(xMin, yMin, zMax, 1.0F);
        vec2.transform(pose);
        vec3 = new Vector4f(xMax, yMin, zMax, 1.0F);
        vec3.transform(pose);
        vec4 = new Vector4f(xMax, yMin, zMin, 1.0F);
        vec4.transform(pose);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);
    }

    private static void drawClosingBeam(VertexConsumer builder, Matrix4f normal, Matrix3f pose, float thickness, double distance, double v1, double v2, float ticks, float r, float g, float b, float alpha) {
        Vector3f vector3f = new Vector3f(0.0F, 1.0F, 0.0F);
        vector3f.transform(pose);

        float xMin = -thickness;
        float xMax = thickness;
        float yMin = -thickness - 0.115F;
        float yMax = thickness - 0.115F;
        float zMin = 0;
        float zMax = (float) distance;

        Vector4f vec1 = new Vector4f(xMin, yMin, zMin, 1.0F);
        vec1.transform(normal);
        Vector4f vec2 = new Vector4f(0, 0, zMax, 1.0F);
        vec2.transform(normal);
        Vector4f vec3 = new Vector4f(0, 0, zMax, 1.0F);
        vec3.transform(normal);
        Vector4f vec4 = new Vector4f(xMin, yMax, zMin, 1.0F);
        vec4.transform(normal);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMax, yMin, zMin, 1.0F);
        vec1.transform(normal);
        vec2 = new Vector4f(0, 0, zMax, 1.0F);
        vec2.transform(normal);
        vec3 = new Vector4f(0, 0, zMax, 1.0F);
        vec3.transform(normal);
        vec4 = new Vector4f(xMax, yMax, zMin, 1.0F);
        vec4.transform(normal);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMin, yMax, zMin, 1.0F);
        vec1.transform(normal);
        vec2 = new Vector4f(0, 0, zMax, 1.0F);
        vec2.transform(normal);
        vec3 = new Vector4f(0, 0, zMax, 1.0F);
        vec3.transform(normal);
        vec4 = new Vector4f(xMax, yMax, zMin, 1.0F);
        vec4.transform(normal);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);

        vec1 = new Vector4f(xMin, yMin, zMin, 1.0F);
        vec1.transform(normal);
        vec2 = new Vector4f(0, 0, zMax, 1.0F);
        vec2.transform(normal);
        vec3 = new Vector4f(0, 0, zMax, 1.0F);
        vec3.transform(normal);
        vec4 = new Vector4f(xMax, yMin, zMin, 1.0F);
        vec4.transform(normal);
        drawQuad(builder, (float) v1, (float) v2, r, g, b, alpha, vector3f, vec1, vec2, vec3, vec4);
    }

    private static void drawQuad(VertexConsumer builder, float v1, float v2, float r, float g, float b, float alpha, Vector3f vector3f, Vector4f vec1, Vector4f vec2, Vector4f vec3, Vector4f vec4) {
        builder.vertex(vec4.x(), vec4.y(), vec4.z(), r, g, b, alpha, 0, v1, OverlayTexture.NO_OVERLAY, 15728880, vector3f.x(), vector3f.y(), vector3f.z());
        builder.vertex(vec3.x(), vec3.y(), vec3.z(), r, g, b, alpha, 0, v2, OverlayTexture.NO_OVERLAY, 15728880, vector3f.x(), vector3f.y(), vector3f.z());
        builder.vertex(vec2.x(), vec2.y(), vec2.z(), r, g, b, alpha, 1, v2, OverlayTexture.NO_OVERLAY, 15728880, vector3f.x(), vector3f.y(), vector3f.z());
        builder.vertex(vec1.x(), vec1.y(), vec1.z(), r, g, b, alpha, 1, v1, OverlayTexture.NO_OVERLAY, 15728880, vector3f.x(), vector3f.y(), vector3f.z());
    }
}