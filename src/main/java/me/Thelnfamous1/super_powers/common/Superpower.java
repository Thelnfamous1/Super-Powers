package me.Thelnfamous1.super_powers.common;

import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public enum Superpower implements StringRepresentable {
    NONE(0, "none", DyeColor.WHITE),
    LIGHTNING(1, "lightning", DyeColor.LIGHT_BLUE),
    FIRE(2, "fire", DyeColor.ORANGE),
    ICE(3, "ice", DyeColor.LIGHT_BLUE),
    EARTH(4, "earth", DyeColor.GREEN),
    TELEKINESIS(5, "telekinesis", DyeColor.PURPLE);

    public static final StringRepresentable.EnumCodec<Superpower> CODEC = StringRepresentable.fromEnum(Superpower::values);
    private static final Superpower[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Superpower::getId)).toArray(Superpower[]::new);

    private final String key;
    private final DyeColor dyeColor;
    private final int id;

    Superpower(int id, String key, DyeColor dyeColor){
        this.id = id;
        this.key = key;
        this.dyeColor = dyeColor;
    }

    public static Superpower byId(int id) {
        return BY_ID[id % BY_ID.length];
    }

    @Nullable
    public static Superpower byName(String name){
        return CODEC.byName(name);
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


}
