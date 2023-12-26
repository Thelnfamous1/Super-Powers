package me.Thelnfamous1.super_powers.common;

import com.mojang.datafixers.util.Either;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public enum Superpower implements StringRepresentable {
    NONE(0, "none", DyeColor.WHITE),
    LIGHTNING(1, "lightning", DyeColor.LIGHT_BLUE, SuperpowerHelper::castLightning, SuperpowerHelper::fireBeam, SuperpowerHelper::tickLightning),
    FIRE(2, "fire", DyeColor.ORANGE, SuperpowerHelper::castFireball, SuperpowerHelper::fireBeam, SuperpowerHelper::tickFire),
    ICE(3, "ice", DyeColor.LIGHT_BLUE, SuperpowerHelper::castSnowball, SuperpowerHelper::fireBeam, SuperpowerHelper::tickIce),
    EARTH(4, "earth", DyeColor.GREEN, SuperpowerHelper::castBonemeal),
    TELEKINESIS(5, "telekinesis", DyeColor.PURPLE, SuperpowerHelper::castTelekinesis, SuperpowerHelper::castNone, SuperpowerHelper::tickTelekinesis);

    public static final StringRepresentable.EnumCodec<Superpower> CODEC = StringRepresentable.fromEnum(Superpower::values);
    private static final Superpower[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Superpower::getId)).toArray(Superpower[]::new);

    private final String key;
    private final DyeColor dyeColor;
    private final Consumer<Player> primary;
    private final Consumer<Player> secondary;
    private final TickConsumer tickUse;
    private final int id;

    Superpower(int id, String key, DyeColor dyeColor){
        this(id, key, dyeColor, SuperpowerHelper::castNone, SuperpowerHelper::castNone, SuperpowerHelper::noTick);
    }

    Superpower(int id, String key, DyeColor dyeColor, Consumer<Player> primary){
        this(id, key, dyeColor, primary, SuperpowerHelper::castNone, SuperpowerHelper::noTick);
    }

    Superpower(int id, String key, DyeColor dyeColor, Consumer<Player> primary, Consumer<Player> secondary, TickConsumer tickUse){
        this.id = id;
        this.key = key;
        this.dyeColor = dyeColor;
        this.primary = primary;
        this.secondary = secondary;
        this.tickUse = tickUse;
    }

    public static Superpower byId(int id) {
        return BY_ID[id % BY_ID.length];
    }

    @Nullable
    public static Superpower byName(String name){
        return CODEC.byName(name);
    }

    public void activatePrimary(Player player){
        this.primary.accept(player);
    }

    public void activateSecondary(Player player){
        this.secondary.accept(player);
    }

    public void tickUse(Entity shooter, Either<EntityHitResult, BlockHitResult> hitResultEither, int ticksFiringBeam){
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

    public boolean isNone(){
        return this == NONE;
    }

    @FunctionalInterface
    public interface TickConsumer{
        void apply(Entity shooter, Either<EntityHitResult, BlockHitResult> hitResult, int ticksFiringBeam);
    }


}
