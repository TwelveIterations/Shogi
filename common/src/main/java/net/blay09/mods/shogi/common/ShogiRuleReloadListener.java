package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepository;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShogiRuleReloadListener extends SimplePreparableReloadListener<List<ShogiRuleReloadListener.ScopeRules>> {

    private final HolderLookup.Provider registries;
    private final Path configDirectory;

    public ShogiRuleReloadListener(HolderLookup.Provider registries, Path configDirectory) {
        this.registries = registries;
        this.configDirectory = configDirectory;
    }

    @Override
    protected List<ScopeRules> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        final List<ScopeRules> loadedRules = new ArrayList<>();
        for (final var scope : ShogiScopeRegistry.getAll()) {
            final var repository = ShogiRuleRepositories.get(scope).orElse(null);
            if (repository == null) {
                continue;
            }

            final var configRules = ShogiRuleLoader.loadConfigRules(registries, scope, configDirectory);
            final var datapackRules = ShogiRuleLoader.loadDatapackRules(registries, scope, resourceManager);
            loadedRules.add(new ScopeRules(repository, configRules, datapackRules));
        }
        return loadedRules;
    }

    @Override
    protected void apply(List<ScopeRules> loadedRules, ResourceManager resourceManager, ProfilerFiller profiler) {
        loadedRules.forEach(it -> it.repository().apply(it.configRules(), it.datapackRules()));
    }

    protected record ScopeRules(ShogiRuleRepository repository, Map<Identifier, ShogiEffect<?>> configRules, Map<Identifier, ShogiEffect<?>> datapackRules) {
    }
}
