package me.Thelnfamous1.super_powers.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.Thelnfamous1.super_powers.SuperPowers;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityInterface;
import me.Thelnfamous1.super_powers.common.network.S2CUpdateSuperpowerPacket;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.List;

public class SPCommands {

    public static final String COMMANDS_SUPERPOWER_QUERY = "commands.%s.superpower.query".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_GIVE_FAILURE = "commands.%s.superpower.give.failure".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_GIVE_SUCCESS = "commands.%s.superpower.give.success".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_GIVE_ALL = "commands.%s.superpower.give.all".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_REMOVE_FAILURE = "commands.%s.superpower.remove.failure".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_REMOVE_SUCCESS = "commands.%s.superpower.remove.success".formatted(SuperPowers.MODID);
    public static final String COMMANDS_SUPERPOWER_REMOVE_ALL = "commands.%s.superpower.remove.all".formatted(SuperPowers.MODID);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("superpower");

        baseCommand.then(Commands.literal("get")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes((context) -> getSuperpowers(context, EntityArgument.getPlayer(context, "player")))));


        baseCommand.then(Commands.literal("give")
                .then(Commands.literal("all")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes((context) -> giveAllSuperpowers(context.getSource(), EntityArgument.getPlayer(context, "player"))))));


        baseCommand.then(Commands.literal("remove")
                .then(Commands.literal("all")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes((context) -> removeAllSuperpowers(context.getSource(), EntityArgument.getPlayer(context, "player"))))));

        for(Superpower superpower : Superpower.values()) {
            baseCommand.then(Commands.literal("give")
                    .then(Commands.literal(superpower.getSerializedName())
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes((context) -> giveSuperpower(context.getSource(), superpower, EntityArgument.getPlayer(context, "player")))
                            )
                    )
            ).then(Commands.literal("remove")
                    .then(Commands.literal(superpower.getSerializedName())
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes((context) -> removeSuperpower(context.getSource(), superpower, EntityArgument.getPlayer(context, "player")))
                            )
                    )
            );
        }

        dispatcher.register(baseCommand.requires((stack) -> stack.hasPermission(2)));
    }

    private static int getSuperpowers(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        Collection<Superpower> superpowers = SuperpowerCapability.getCapability(player).getSuperpowers();
        List<MutableComponent> superpowerComponents = superpowers.stream().map(Superpower::getColoredDisplayName).toList();
        MutableComponent queryComponent = Component.translatable(COMMANDS_SUPERPOWER_QUERY, player.getDisplayName());
        queryComponent.append("[");
        for(int i = 0; i < superpowerComponents.size(); i++){
            queryComponent.append(superpowerComponents.get(i));
            if(i < superpowerComponents.size() - 1){
                queryComponent.append(", ");
            }
        }
        queryComponent.append("]");
        context.getSource().sendSuccess(queryComponent, false);
        return 0;
    }

    public static int giveAllSuperpowers(CommandSourceStack pSource, ServerPlayer player) {
        SuperpowerCapabilityInterface capability = SuperpowerCapability.getCapability(player);
        for(Superpower superpower : Superpower.values()){
            capability.addSuperpower(superpower);
        }

        SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CUpdateSuperpowerPacket(player, capability));
        pSource.sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_GIVE_ALL, player.getDisplayName()), true);
        return 0;
    }

    public static int removeAllSuperpowers(CommandSourceStack pSource, ServerPlayer player) {
        SuperpowerCapabilityInterface capability = SuperpowerCapability.getCapability(player);
        for(Superpower superpower : Superpower.values()){
            capability.removeSuperpower(superpower);
        }

        SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CUpdateSuperpowerPacket(player, capability));
        pSource.sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_REMOVE_ALL, player.getDisplayName()), true);
        return 0;
    }

    public static int giveSuperpower(CommandSourceStack pSource, Superpower superpower, ServerPlayer player) throws CommandSyntaxException {
        SuperpowerCapabilityInterface capability = SuperpowerCapability.getCapability(player);
        if (!capability.addSuperpower(superpower)) {
            throw new DynamicCommandExceptionType((key) -> Component.translatable(COMMANDS_SUPERPOWER_GIVE_FAILURE, player.getDisplayName(), key)).create(superpower.getSerializedName());
        } else {
            SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CUpdateSuperpowerPacket(player, capability));
            pSource.sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_GIVE_SUCCESS, player.getDisplayName(), superpower.getColoredDisplayName()), true);
            return 0;
        }
    }

    public static int removeSuperpower(CommandSourceStack pSource, Superpower superpower, ServerPlayer player) throws CommandSyntaxException {
        SuperpowerCapabilityInterface capability = SuperpowerCapability.getCapability(player);
        if (!capability.removeSuperpower(superpower)) {
            throw new DynamicCommandExceptionType((key) -> Component.translatable(COMMANDS_SUPERPOWER_REMOVE_FAILURE, player.getDisplayName(), key)).create(superpower.getSerializedName());
        } else {
            SPNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CUpdateSuperpowerPacket(player, capability));
            pSource.sendSuccess(Component.translatable(COMMANDS_SUPERPOWER_REMOVE_SUCCESS, player.getDisplayName(), superpower.getColoredDisplayName()), true);
            return 0;
        }
    }
}
