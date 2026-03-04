package net.blay09.mods.shogi.scope.internal;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.ShogiEmpty;
import net.blay09.mods.shogi.scope.ShogiNetworkCache;
import net.blay09.mods.shogi.scope.ShogiOverrideProvider;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ShogiScopeImpl implements ShogiScope {
    private final Identifier identifier;
    private List<String> defaultNamespaces;

    public ShogiScopeImpl(Identifier identifier) {
        this.identifier = identifier;
        this.defaultNamespaces = List.of(identifier.getNamespace());
    }

    record ShogiEffectType(Identifier identifier, MapCodec<? extends ShogiEffect<?>> mapCodec) {
    }

    private final Map<Identifier, ShogiEffectType> effectTypeById = new HashMap<>();
    private final Map<Identifier, List<String>> ordinalParametersById = new HashMap<>();
    private final List<ShogiOverrideProvider> overrideProviders = new ArrayList<>();
    private final Codec<ShogiEffectType> effectTypeByNameCodec = Identifier.CODEC.flatXmap((identifier) -> Optional.ofNullable(effectTypeById.get(identifier))
            .map(DataResult::success)
            .orElseGet(() -> DataResult.error(() -> "Unknown effect: " + identifier)), (type) -> DataResult.success(type.identifier()));
    private final Codec<ShogiEffect<?>> effectCodec = effectTypeByNameCodec.dispatch(it -> effectTypeById.get(it.identifier()), ShogiEffectType::mapCodec);

    private ShogiNetworkCache cache = ShogiNetworkCache.NONE;

    @Override
    public Identifier identifier() {
        return identifier;
    }

    @Override
    public void setNetworkCache(ShogiNetworkCache cache) {
        this.cache = cache;
    }

    @Override
    public ShogiNetworkCache getNetworkCache() {
        return cache;
    }

    @Override
    public void registerEffect(Identifier id, MapCodec<? extends ShogiEffect<?>> effectCodec, List<String> ordinalParameters) {
        effectTypeById.put(id, new ShogiEffectType(id, effectCodec));
        ordinalParametersById.put(id, List.copyOf(ordinalParameters));
    }

    @Override
    public Codec<ShogiEffect<?>> getEffectCodec() {
        return effectCodec;
    }

    @Override
    public List<String> getOrdinalParameters(Identifier identifier) {
        return ordinalParametersById.getOrDefault(identifier, List.of());
    }

    @Override
    public boolean hasEffect(Identifier identifier) {
        return effectTypeById.containsKey(identifier);
    }

    @Override
    public List<String> getDefaultNamespaces() {
        return defaultNamespaces;
    }

    @Override
    public void setDefaultNamespaces(List<String> namespaces) {
        final Set<String> deduplicated = new LinkedHashSet<>();
        for (final var namespace : namespaces) {
            if (!Identifier.isValidNamespace(namespace)) {
                throw new IllegalArgumentException("Invalid namespace: " + namespace);
            }
            deduplicated.add(namespace);
        }
        defaultNamespaces = List.copyOf(deduplicated);
    }

    @Override
    public <TContext, TSuccess> Either<?, ?> resolve(Identifier identifier, TContext context, Function<TContext, TSuccess> defaultProvider) {
        final var normalizedContext = ShogiContext.of(context);
        final var cached = cache.getRemoteValue(identifier, normalizedContext);
        if (cached.isPresent()) {
            return cached.get();
        }

        return cache.valueResolved(identifier, normalizedContext, resolveWithoutCache(identifier, normalizedContext, context, defaultProvider));
    }

    private <TContext, TSuccess> Either<?, ?> resolveWithoutCache(Identifier identifier, ShogiContext normalizedContext, TContext context, Function<TContext, TSuccess> defaultProvider) {
        final var override = getOverride(identifier);
        if (override != null) {
            try {
                var result = override.apply(normalizedContext);
                if (!(result.right().orElse(null) instanceof ShogiEmpty)) {
                    return result;
                }
            } catch (Throwable t) {
                return Either.right(t);
            }
        }

        try {
            return Either.left(defaultProvider.apply(context));
        } catch (Throwable t) {
            return Either.right(t);
        }
    }

    @Override
    public void registerOverrideProvider(ShogiOverrideProvider provider) {
        overrideProviders.add(provider);
    }

    @Nullable
    private ShogiEffect<?> getOverride(Identifier identifier) {
        ShogiEffect<?> override = null;
        for (final var provider : overrideProviders) {
            final var providerOverride = provider.getOverride(identifier).orElse(null);
            if (providerOverride != null) {
                override = providerOverride;
            }
        }
        return override;
    }


}
