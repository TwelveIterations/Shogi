package net.blay09.mods.shogi.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldowns;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldownsAccess;
import net.minecraft.commands.CommandPermissionLevels;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.IntegerArgument;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.commands.suggestions.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;

public final class ShogiCommand {

    private ShogiCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shogi")
                .requires(Commands.hasPermission(CommandPermissionLevels.LEVEL_GAMEMASTERS))
                .then(Commands.literal("cooldown")
                        .then(Commands.literal("reset")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.literal("all")
                                                .executes(context -> resetAllCooldowns(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                                        .then(Commands.argument("identifier", IdentifierArgument.id())
                                                .suggests(SuggestionProviders.serverOnly((server, context, builder) -> {
                                                    final var targets = EntityArgument.getSuggestedEntities(server, context, "targets")
                                                            .filter(ServerPlayer.class::isInstance)
                                                            .map(ServerPlayer.class::cast)
                                                            .toList();
                                                    final var keys = new HashSet<String>();
                                                    for (final var target : targets) {
                                                        final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
                                                        for (final var cooldown : cooldowns.getCooldowns()) {
                                                            keys.add(cooldown.identifier().toString());
                                                        }
                                                    }
                                                    return SuggestionContext.suggest(keys, builder);
                                                }))
                                                .executes(context -> resetCooldown(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        IdentifierArgument.getId(context, "identifier"))))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("identifier", IdentifierArgument.id())
                                                .then(Commands.argument("seconds", IntegerArgument.integer())
                                                        .executes(context -> addCooldown(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IdentifierArgument.getId(context, "identifier"),
                                                                IntegerArgument.getInteger(context, "seconds")))))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("identifier", IdentifierArgument.id())
                                                .then(Commands.argument("seconds", IntegerArgument.integer(0))
                                                        .executes(context -> setCooldown(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IdentifierArgument.getId(context, "identifier"),
                                                                IntegerArgument.getInteger(context, "seconds")))))))));
    }

    private static int resetAllCooldowns(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int removed = 0;
        for (final var target : targets) {
            final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
            removed += cooldowns.getCooldowns().size();
            cooldowns.resetAllCooldowns();
        }

        source.sendSuccess(() -> Component.translatable("commands.shogi.cooldown.reset.all.success", targets.size()), true);
        return removed;
    }

    private static int resetCooldown(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier identifier) {
        int removed = 0;
        for (final var target : targets) {
            final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
            cooldowns.resetCooldown(identifier);
            removed++;
        }

        source.sendSuccess(() -> Component.translatable("commands.shogi.cooldown.reset.success", identifier.toString(), targets.size()), true);
        return removed;
    }

    private static int addCooldown(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier identifier, int seconds) {
        final var ticks = secondsToTicks(seconds);
        for (final var target : targets) {
            ShogiCooldowns.get(target).addCooldown(identifier, ticks);
        }

        source.sendSuccess(() -> Component.translatable("commands.shogi.cooldown.add.success", seconds, identifier.toString(), targets.size()), true);
        return targets.size();
    }

    private static int setCooldown(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier identifier, int seconds) {
        final var ticks = secondsToTicks(seconds);
        for (final var target : targets) {
            ShogiCooldowns.get(target).setCooldown(identifier, ticks);
        }

        source.sendSuccess(() -> Component.translatable("commands.shogi.cooldown.set.success", identifier.toString(), seconds, targets.size()), true);
        return targets.size();
    }

    private static int secondsToTicks(int seconds) {
        return Math.clamp(seconds * 20L, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
