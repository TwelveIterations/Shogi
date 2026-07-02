package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.common.network.ShogiDefaultStreamCodecs;
import net.blay09.mods.shogi.common.platform.ShogiEventListener;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ShogiCommon implements ShogiEventListener {

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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shogi", path);
    }
}
