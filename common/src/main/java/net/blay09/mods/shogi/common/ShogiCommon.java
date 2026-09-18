package net.blay09.mods.shogi.common;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.common.network.ShogiDefaultStreamCodecs;
import net.blay09.mods.shogi.common.platform.ShogiEventListener;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ShogiCommon implements ShogiEventListener {

    private static final ShogiValue<LivingEntity, ?> ON_DEATH = Shogi.defaultScope().maybe(
            id("on_death"),
            _ -> Either.right(EmptyEffect.INSTANCE)
    );

    public ShogiCommon() {
        ShogiDefaultStreamCodecs.registerDefaults();

        final var runtime = ShogiRuntimeSpi.get();
        final var configDirectory = runtime.getConfigDirectory();
        runtime.registerServerReloadListener(ShogiCommon.id("rule_reloader"), registries -> new ShogiRuleReloadListener(registries, configDirectory));
    }

    public static ShogiCommon initialize() {
        return new ShogiCommon();
    }

    @Override
    public void onPlayerDisconnected(Player player) {
        for (final var scope : ShogiScopeRegistry.getAll()) {
            scope.getNetworkCache().invalidate(player);
        }
    }

    @Override
    public void onLivingEntityDeath(LivingEntity entity, DamageSource source) {
        ON_DEATH.get(entity);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("shogi", path);
    }
}
