package net.blay09.mods.shogi.fabric.gametest;

import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.effect.server.cooldown.AddCooldown;
import net.blay09.mods.shogi.common.effect.server.cooldown.ShogiCooldowns;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShogiGameTest {

    @GameTest
    public void onDeathIsEvaluatedForDyingEntity(GameTestHelper helper) {
        final var repository = ShogiRuleRepositories.get(Shogi.defaultScope()).orElseThrow();
        final var entity = helper.spawn(EntityTypes.PIG, 1, 1, 1);
        final var ruleEvaluated = new AtomicBoolean();

        repository.apply(Map.of(ShogiCommon.id("on_death"), ShogiEffect.simple(
                ShogiCommon.id("test_on_death"),
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
        final var cooldown = ShogiCommon.id("test_death_cooldown");
        final var player = helper.makeMockServerPlayerInLevel();

        repository.apply(Map.of(ShogiCommon.id("on_death"), new AddCooldown(cooldown, ConstantEffect.of(30))), Map.of());

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
