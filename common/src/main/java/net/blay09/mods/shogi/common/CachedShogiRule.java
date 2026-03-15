package net.blay09.mods.shogi.common;

import com.mojang.serialization.JsonOps;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CachedShogiRule {

    private static final Logger logger = LoggerFactory.getLogger(CachedShogiRule.class);

    private final Function<HolderLookup.Provider, ShogiEffect<?>> compiler;
    private volatile @Nullable ShogiEffect<?> cachedEffect;

    private CachedShogiRule(Function<HolderLookup.Provider, ShogiEffect<?>> compiler) {
        this.compiler = compiler;
    }

    public static CachedShogiRule ofRule(ShogiScope scope, Supplier<String> ruleSupplier) {
        return new CachedShogiRule((registries) -> compileRule(scope, registries, ruleSupplier.get()));
    }

    public static CachedShogiRule ofRules(ShogiScope scope, Supplier<List<String>> rulesSupplier) {
        return new CachedShogiRule((registries) -> compileRules(scope, registries, rulesSupplier.get()));
    }

    public ShogiEffect<?> get(HolderLookup.Provider registries) {
        final var effect = cachedEffect;
        if (effect != null) {
            return effect;
        }

        synchronized (this) {
            if (cachedEffect == null) {
                cachedEffect = compiler.apply(registries);
            }
            return cachedEffect;
        }
    }

    public void invalidate() {
        cachedEffect = null;
    }

    private static ShogiEffect<?> compileRule(ShogiScope scope, HolderLookup.Provider registries, @Nullable String rule) {
        if (rule == null || rule.isBlank()) {
            return EmptyEffect.INSTANCE;
        }

        final var registryOps = RegistryOps.create(JsonOps.INSTANCE, registries);
        return ShogiRuleParser.parse(scope, registryOps, rule)
                .resultOrPartial(error -> logger.warn("Skipping cached Shogi rule: {}", error))
                .orElse(EmptyEffect.INSTANCE);
    }

    private static ShogiEffect<?> compileRules(ShogiScope scope, HolderLookup.Provider registries, @Nullable List<String> rules) {
        final var registryOps = RegistryOps.create(JsonOps.INSTANCE, registries);
        final List<ShogiEffect<?>> compiledRules = new ArrayList<>();
        if (rules != null) {
            for (final var rule : rules) {
                if (!rule.isBlank()) {
                    final var parsed = ShogiRuleParser.parse(scope, registryOps, rule);
                    parsed.error().ifPresent(error -> logger.warn("Skipping Shogi rule {}: {}", rule, error));
                    parsed.result().ifPresent(compiledRules::add);
                }
            }
        }

        return AggregateEffect.withAutoApplied(scope, registryOps, compiledRules);
    }
}
