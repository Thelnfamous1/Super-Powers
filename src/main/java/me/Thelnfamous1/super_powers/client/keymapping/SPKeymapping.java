package me.Thelnfamous1.super_powers.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import me.Thelnfamous1.super_powers.SuperPowers;
import net.minecraft.client.KeyMapping;

public class SPKeymapping {
    public static final KeyMapping USE_SUPERPOWER = new KeyMapping("key.%s.use_superpower".formatted(SuperPowers.MODID), InputConstants.KEY_V, "key.category.%s".formatted(SuperPowers.MODID));
}
