package net.blay09.mods.shogi.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldownsAccess;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
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
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("cooldown")
                        .then(Commands.literal("reset")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.literal("all")
                                                .executes(context -> resetAllCooldowns(context.getSource(), EntityArgument.getPlayers(context, "targets"))))
                                        .then(Commands.argument("identifier", IdentifierArgument.id())
                                                .suggests((context, builder) -> {
                                                    final var targets = EntityArgument.getPlayers(context, "targets");
                                                    final var keys = new HashSet<String>();
                                                    for (final var target : targets) {
                                                        final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
                                                        for (final var cooldown : cooldowns.getCooldowns()) {
                                                            keys.add(cooldown.identifier().toString());
                                                        }
                                                    }
                                                    return SharedSuggestionProvider.suggest(keys, builder);
                                                })
                                                .executes(context -> resetCooldown(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        IdentifierArgument.getId(context, "identifier"))))))));
    }

    private static int resetAllCooldowns(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int removed = 0;
        for (final var target : targets) {
            final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
            removed += cooldowns.getCooldowns().size();
            cooldowns.resetAllCooldowns();
        }

        source.sendSuccess(() -> Component.literal("Reset all cooldown(s) across " + targets.size() + " player(s)."), true);
        return removed;
    }

    private static int resetCooldown(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier identifier) {
        int removed = 0;
        for (final var target : targets) {
            final var cooldowns = ((ShogiCooldownsAccess) target).shogi$getCooldowns();
            cooldowns.resetCooldown(identifier);
            removed++;
        }

        source.sendSuccess(() -> Component.literal("Reset cooldown(s) matching " + identifier + " across " + targets.size() + " player(s)."), true);
        return removed;
    }
}
