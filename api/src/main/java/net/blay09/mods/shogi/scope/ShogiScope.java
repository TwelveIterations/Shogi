package net.blay09.mods.shogi.scope;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registry and resolution boundary for Shogi effects and value overrides.
 */
public interface ShogiScope {
    /**
     * Returns this scope's identifier.
     *
     * @return scope identifier
     */
    Identifier identifier();

    /**
     * Registers a simple, infallible effect with no parameters.
     *
     * @param id effect identifier
     * @param supplier result supplier
     */
    default <T> void registerSimpleEffect(Identifier id, Supplier<T> supplier) {
        final var effect = ShogiEffect.simple(id, supplier);
        registerEffect(id, MapCodec.unit(effect), List.of());
    }

    /**
     * Registers a simple, infallible effect with no parameters.
     *
     * @param id effect identifier
     * @param function result function
     */
    default <T> void registerSimpleEffect(Identifier id, Function<ShogiContext, T> function) {
        final var effect = ShogiEffect.simple(id, function);
        registerEffect(id, MapCodec.unit(effect), List.of());
    }

    /**
     * Registers an effect codec without ordinal parameter aliases.
     *
     * @param id effect identifier
     * @param effectCodec effect codec
     */
    default void registerEffect(Identifier id, MapCodec<? extends ShogiEffect<?>> effectCodec) {
        registerEffect(id, effectCodec, List.of());
    }

    /**
     * Registers an effect codec with optional ordinal parameter aliases.
     *
     * @param id effect identifier
     * @param effectCodec effect codec
     * @param ordinalParameters parameter names used for positional argument decoding
     */
    void registerEffect(Identifier id, MapCodec<? extends ShogiEffect<?>> effectCodec, List<String> ordinalParameters);

    /**
     * Returns the polymorphic effect codec for all effects registered in this scope.
     *
     * @return effect codec
     */
    Codec<ShogiEffect<?>> getEffectCodec();

    /**
     * Returns ordinal parameter names for the given effect identifier.
     *
     * @param identifier effect identifier
     * @return ordered list of ordinal parameter names
     */
    List<String> getOrdinalParameters(Identifier identifier);

    /**
     * Returns whether an effect is registered for the given identifier.
     *
     * @param identifier effect identifier
     * @return true if the effect is registered in this scope
     */
    boolean hasEffect(Identifier identifier);

    /**
     * Returns ordered default namespaces used for unqualified identifiers.
     *
     * @return ordered default namespaces
     */
    List<String> getDefaultNamespaces();

    /**
     * Replaces ordered default namespaces used for unqualified identifiers.
     *
     * @param namespaces ordered namespaces
     */
    void setDefaultNamespaces(List<String> namespaces);

    /**
     * Resolves a value for the given identifier and context.
     *
     * @param identifier value identifier
     * @param context resolution context input
     * @param defaultProvider fallback provider used when no override applies
     * @param <TContext> context type
     * @param <TSuccess> default provider success type
     * @return either resolved success or failure payload
     */
    <TContext, TSuccess> Either<?, ?> resolve(Identifier identifier, TContext context, Function<TContext, TSuccess> defaultProvider);

    /**
     * Registers an override provider consulted during resolution.
     *
     * @param provider override provider
     */
    void registerOverrideProvider(ShogiOverrideProvider provider);

    /**
     * Sets the network cache used for synchronized values.
     *
     * @param cache network cache implementation
     */
    void setNetworkCache(ShogiNetworkCache cache);

    /**
     * Returns the active network cache.
     *
     * @return network cache implementation
     */
    ShogiNetworkCache getNetworkCache();
}
