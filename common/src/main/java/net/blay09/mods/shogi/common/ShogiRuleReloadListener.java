package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.common.scope.ShogiOverrideProviderImpl;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ShogiRuleReloadListener implements PreparableReloadListener {
    public static final Map<ShogiScope, ShogiOverrideProviderImpl> overrideProviders = Collections.synchronizedMap(new WeakHashMap<>());

    private final HolderLookup.Provider registries;
    private final Path path;

    public ShogiRuleReloadListener(HolderLookup.Provider registries, Path path) {
        this.registries = registries;
        this.path = path;
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor reloadExecutor, PreparationBarrier preparationBarrier, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(this::loadAllOverrides, reloadExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(this::applyAllOverrides, applyExecutor);
    }

    private List<ScopeOverrides> loadAllOverrides() {
        final List<ScopeOverrides> loadedOverrides = new ArrayList<>();
        for (final var scope : ShogiScopeRegistry.getAll()) {
            final var overrideProvider = overrideProviders.get(scope);
            if (overrideProvider == null) {
                continue;
            }

            final var overrides = ShogiRuleLoader.loadJson(registries, scope, path);
            loadedOverrides.add(new ScopeOverrides(overrideProvider, overrides));
        }
        return loadedOverrides;
    }

    private void applyAllOverrides(List<ScopeOverrides> loadedOverrides) {
        loadedOverrides.forEach(it -> it.overrideProvider().apply(it.overrides()));
    }

    private record ScopeOverrides(ShogiOverrideProviderImpl overrideProvider, Map<Identifier, ShogiEffect<?>> overrides) {
    }
}
