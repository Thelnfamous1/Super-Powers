package me.Thelnfamous1.super_powers.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SPCommands {

    public static final String COMMANDS_SUPERPOWER_QUERY = "commands.%s.superpower.query".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_FAILURE = "commands.%s.superpower.failure".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_SUCCESS = "commands.%s.superpower.success".formatted(SuperPowers.MODID);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("superpower");
        for(Superpower superpower : Superpower.values()) {
            baseCommand.then(Commands.literal("give")
                    .then(Commands.literal(superpower.getSerializedName())
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes((context) -> setSuperpower(context.getSource(), superpower, EntityArgument.getPlayer(context, "player")))
                            )
                    )
            );
        }

        dispatcher.register(baseCommand.requires((stack) -> stack.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes((context) -> getSuperpower(context, EntityArgument.getPlayer(context, "player"))))));
    }

    private static int getSuperpower(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        Superpower superpower = SuperpowerCapability.getCapability(player).getSuperpower();
        context.getSource().sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_QUERY, player.getDisplayName(), superpower.getColoredDisplayName()), false);
        return superpower.getId();
    }

    public static int setSuperpower(CommandSourceStack pSource, Superpower superpower, ServerPlayer player) throws CommandSyntaxException {
        SuperpowerCapabilityInterface capability = SuperpowerCapability.getCapability(player);
        if (capability.getSuperpower() == superpower) {
            throw new DynamicCommandExceptionType((key) -> Component.translatable(COMMANDS_SUPERPOWER_FAILURE, player.getDisplayName(), key)).create(superpower.getSerializedName());
        } else {
            capability.setSuperpower(superpower);
            pSource.sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_SUCCESS, player.getDisplayName(), superpower.getColoredDisplayName()), true);
            return 0;
        }
    }
}
