package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepository;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract class AbstractShogiRuleReloadListener extends SimplePreparableReloadListener<List<AbstractShogiRuleReloadListener.ScopeRules>> {

    private final Supplier<HolderLookup.@Nullable Provider> registriesSupplier;
    private final Path configDirectory;
    private final Predicate<ShogiScope> scopeFilter;

    protected AbstractShogiRuleReloadListener(Supplier<HolderLookup.@Nullable Provider> registriesSupplier, Path configDirectory, Predicate<ShogiScope> scopeFilter) {
        this.registriesSupplier = registriesSupplier;
        this.configDirectory = configDirectory;
        this.scopeFilter = scopeFilter;
    }

    @Override
    protected List<ScopeRules> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        final var registries = registriesSupplier.get();
        if (registries == null) {
            return List.of();
        }

        final List<ScopeRules> loadedRules = new ArrayList<>();
        for (final var scope : ShogiScopeRegistry.getAll()) {
            if (!scopeFilter.test(scope)) {
                continue;
            }

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
