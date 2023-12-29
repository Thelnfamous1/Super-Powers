package me.Thelnfamous1.super_powers.common;

import com.mojang.datafixers.util.Either;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public enum Superpower implements StringRepresentable {
    LIGHTNING(0, "lightning", DyeColor.WHITE, SuperpowerHelper::activateLightning, SuperpowerHelper::tickLightning, SuperpowerHelper::deactivateLightning),
    FIRE(1, "fire", DyeColor.ORANGE, SuperpowerHelper::activateFire, SuperpowerHelper::tickFire, SuperpowerHelper::deactivateFire),
    ICE(2, "ice", DyeColor.LIGHT_BLUE, SuperpowerHelper::activateIce, SuperpowerHelper::tickIce, SuperpowerHelper::deactivateIce),
    EARTH(3, "earth", DyeColor.GREEN, SuperpowerHelper::activateEarth, SuperpowerHelper::tickEarth, SuperpowerHelper::deactivateEarth),
    TELEKINESIS(4, "telekinesis", DyeColor.PURPLE, SuperpowerHelper::activateTelekinesis, SuperpowerHelper::tickTelekinesis, SuperpowerHelper::deactivateTelekinesis);

    public static final StringRepresentable.EnumCodec<Superpower> CODEC = StringRepresentable.fromEnum(Superpower::values);
    private static final Superpower[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Superpower::getId)).toArray(Superpower[]::new);

    private final String key;
    private final DyeColor dyeColor;
    private final Function<Player, Boolean> activatePrimary;
    private final Consumer<Player> deactivate;
    private final TickConsumer tickUse;
    private final int id;

    Superpower(int id, String key, DyeColor dyeColor, Function<Player, Boolean> activatePrimary, TickConsumer tickUse, Consumer<Player> deactivate){
        this.id = id;
        this.key = key;
        this.dyeColor = dyeColor;
        this.activatePrimary = activatePrimary;
        this.deactivate = deactivate;
        this.tickUse = tickUse;
    }

    public static Superpower byId(int id) {
        return BY_ID[id % BY_ID.length];
    }

    @Nullable
    public static Superpower byName(String name){
        return CODEC.byName(name);
    }

    public boolean activate(Player player){
        return this.activatePrimary.apply(player);
    }

    public void deactivate(Player player){
        this.deactivate.accept(player);
    }

    public void tickUse(Player shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
        this.tickUse.apply(shooter, hitResultEither, ticksFiringBeam);
    }

    public MutableComponent getColoredDisplayName(){
        MutableComponent displayName = this.getDisplayName();
        Style style = displayName.getStyle();
        style = style.withColor(this.dyeColor.getTextColor());
        displayName.setStyle(style);
        return displayName;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("superpower.%s.%s".formatted(SuperPowers.MODID, this.key));
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    @FunctionalInterface
    public interface TickConsumer{
        void apply(Player shooter, Either<EntityHitResult, BlockHitResult> hitResult, int ticksFiringBeam);
    }


}
