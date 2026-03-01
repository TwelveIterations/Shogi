package net.blay09.mods.shogi.scope;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;

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
