package me.Thelnfamous1.super_powers.client.keymapping;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.Superpower;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;

public class SPKeymapping {
    public static final BiMap<KeyMapping, Superpower> SUPERPOWERS_BY_KEY = HashBiMap.create();
    public static final String KEY_CATEGORY = "key.category.%s".formatted(SuperPowers.MODID);
    public static final KeyMapping USE_LIGHTNING = createSuperpowerKeybind(Superpower.LIGHTNING, InputConstants.KEY_Z);
    public static final KeyMapping USE_FIRE = createSuperpowerKeybind(Superpower.FIRE, InputConstants.KEY_X);
    public static final KeyMapping USE_ICE = createSuperpowerKeybind(Superpower.ICE, InputConstants.KEY_C);
    public static final KeyMapping USE_EARTH = createSuperpowerKeybind(Superpower.EARTH, InputConstants.KEY_V);
    public static final KeyMapping USE_TELEKINESIS = createSuperpowerKeybind(Superpower.TELEKINESIS, InputConstants.KEY_B);

    @NotNull
    private static KeyMapping createSuperpowerKeybind(Superpower superpower, int key) {
        KeyMapping keyMapping = new KeyMapping("key.%s.use_%s".formatted(SuperPowers.MODID, superpower.getSerializedName()), key, KEY_CATEGORY);
        SUPERPOWERS_BY_KEY.put(keyMapping, superpower);
        return keyMapping;
    }
}
