package me.Thelnfamous1.super_powers.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SPRenderType extends RenderType {
    public final static ResourceLocation CORE_LOCATION = SuperPowers.location("textures/entity/energy_beam/core.png");
    public final static ResourceLocation MAIN_LOCATION = SuperPowers.location("textures/entity/energy_beam/main.png");
    public final static ResourceLocation GLOW_LOCATION = SuperPowers.location("textures/entity/energy_beam/glow.png");
    // Dummy
    public SPRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static final RenderType ENERGY_BEAM_MAIN = create("energy_beam_main",
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_BEACON_BEAM_SHADER)
                    .setTextureState(new TextureStateShard(MAIN_LOCATION, false, false))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType ENERGY_BEAM_GLOW = create("energy_beam_glow",
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_BEACON_BEAM_SHADER)
                    .setTextureState(new TextureStateShard(GLOW_LOCATION, false, false))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType ENERGY_BEAM_CORE = create("energy_beam_core",
            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_BEACON_BEAM_SHADER)
                    .setTextureState(new TextureStateShard(CORE_LOCATION, false, false))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));
}