package net.blay09.mods.shogi.fabric.gametest;

import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
}
