package net.blay09.mods.shogi.fabric.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.effect.server.cooldown.AddCooldown;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldowns;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShogiGameTest {

    @GameTest
    public void cooldownCommandsModifyPlayerCooldowns(GameTestHelper helper) throws CommandSyntaxException {
        final var player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        final var cooldowns = ShogiCooldowns.get(player);
        final var firstCooldown = Identifier.fromNamespaceAndPath("shogi", "test_command_first");
        final var secondCooldown = Identifier.fromNamespaceAndPath("shogi", "test_command_second");
        final var server = helper.getLevel().getServer();
        final var dispatcher = server.getCommands().getDispatcher();
        final var source = server.createCommandSourceStack().withEntity(player);

        dispatcher.execute("shogi cooldown set @s " + firstCooldown + " 10", source);
        helper.assertValueEqual(cooldowns.getRemainingTicks(firstCooldown), 200L, "set command cooldown ticks");

        dispatcher.execute("shogi cooldown add @s " + firstCooldown + " 5", source);
        helper.assertValueEqual(cooldowns.getRemainingTicks(firstCooldown), 300L, "add command cooldown ticks");

        dispatcher.execute("shogi cooldown set @s " + secondCooldown + " 20", source);
        dispatcher.execute("shogi cooldown reset @s " + firstCooldown, source);
        helper.assertFalse(cooldowns.hasCooldown(firstCooldown), "reset command did not remove the selected cooldown");
        helper.assertTrue(cooldowns.hasCooldown(secondCooldown), "reset command removed an unrelated cooldown");

        dispatcher.execute("shogi cooldown reset @s all", source);
        helper.assertFalse(cooldowns.hasCooldown(secondCooldown), "reset all command did not remove every cooldown");
        helper.succeed();
    }

    @GameTest
    public void onDeathIsEvaluatedForDyingEntity(GameTestHelper helper) {
        final var repository = ShogiRuleRepositories.get(Shogi.defaultScope()).orElseThrow();
        final var entity = helper.spawn(EntityTypes.PIG, 1, 1, 1);
        final var ruleEvaluated = new AtomicBoolean();

        repository.apply(Map.of(Identifier.fromNamespaceAndPath("shogi", "on_death"), ShogiEffect.simple(
                Identifier.fromNamespaceAndPath("shogi", "test_on_death"),
                context -> {
                    final var matchesEntity = context.entity() == entity;
                    ruleEvaluated.set(matchesEntity);
                    return matchesEntity;
                }
        )), Map.of());

        try {
            entity.kill(helper.getLevel());
        } finally {
            repository.apply(Map.of(), Map.of());
        }

        helper.assertTrue(ruleEvaluated.get(), "shogi:on_death was not evaluated for the dying entity");
        helper.succeed();
    }

    @GameTest
    @SuppressWarnings("removal")
    public void cooldownAddedOnDeathPersistsAfterPlayerRespawn(GameTestHelper helper) {
        final var repository = ShogiRuleRepositories.get(Shogi.defaultScope()).orElseThrow();
        final var cooldown = Identifier.fromNamespaceAndPath("shogi", "test_death_cooldown");
        final var player = helper.makeMockServerPlayerInLevel();

        repository.apply(Map.of(Identifier.fromNamespaceAndPath("shogi", "on_death"), new AddCooldown(cooldown, ConstantEffect.of(30))), Map.of());

        try {
            player.die(helper.getLevel().damageSources().genericKill());
            helper.assertTrue(
                    ShogiCooldowns.get(player).hasCooldown(cooldown),
                    "shogi:on_death did not add the cooldown to the dying player"
            );
            final var respawnedPlayer = helper.getLevel().getServer().getPlayerList()
                    .respawn(player, false, Entity.RemovalReason.KILLED);

            helper.assertTrue(
                    ShogiCooldowns.get(respawnedPlayer).hasCooldown(cooldown),
                    "Cooldown added by shogi:on_death was not active after the player respawned"
            );
        } finally {
            repository.apply(Map.of(), Map.of());
        }

        helper.succeed();
    }
}
