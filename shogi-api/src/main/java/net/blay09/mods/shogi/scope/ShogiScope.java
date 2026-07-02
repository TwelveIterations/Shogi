package net.blay09.mods.shogi.scope;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
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
    ResourceLocation identifier();

    /**
     * Registers a simple, infallible effect with no parameters.
     *
     * @param id effect identifier
     * @param supplier result supplier
     */
    default <T> void registerSimpleEffect(ResourceLocation id, Supplier<T> supplier) {
        final var effect = ShogiEffect.simple(id, supplier);
        registerEffect(id, MapCodec.unit(effect), List.of());
    }

    /**
     * Registers a simple, infallible effect with no parameters.
     *
     * @param id effect identifier
     * @param function result function
     */
    default <T> void registerSimpleEffect(ResourceLocation id, Function<ShogiContext, T> function) {
        final var effect = ShogiEffect.simple(id, function);
        registerEffect(id, MapCodec.unit(effect), List.of());
    }

    /**
     * Registers an effect codec without ordinal parameter aliases.
     *
     * @param id effect identifier
     * @param effectCodec effect codec
     */
    default void registerEffect(ResourceLocation id, MapCodec<? extends ShogiEffect<?>> effectCodec) {
        registerEffect(id, effectCodec, List.of());
    }

    /**
     * Registers an alias that resolves to an existing canonical effect identifier.
     *
     * @param alias alternate identifier accepted during lookup and decoding
     * @param target canonical registered effect identifier
     */
    void registerEffectAlias(ResourceLocation alias, ResourceLocation target);

    /**
     * Registers an effect codec with optional ordinal parameter aliases.
     *
     * @param id effect identifier
     * @param effectCodec effect codec
     * @param ordinalParameters parameter names used for positional argument decoding
     */
    void registerEffect(ResourceLocation id, MapCodec<? extends ShogiEffect<?>> effectCodec, List<String> ordinalParameters);

    /**
     * Resolves the given effect identifier to its canonical registered identifier.
     *
     * @param identifier canonical id or alias
     * @return canonical effect identifier, or empty when unresolved
     */
    Optional<ResourceLocation> resolveEffectIdentifier(ResourceLocation identifier);

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
    List<String> getOrdinalParameters(ResourceLocation identifier);

    /**
     * Returns whether an effect is registered for the given identifier.
     *
     * @param identifier effect identifier
     * @return true if the effect is registered in this scope
     */
    boolean hasEffect(ResourceLocation identifier);

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
     * Marks this scope to load rules on the client as well.
     *
     * @return this scope
     */
    ShogiScope loadOnClient();

    /**
     * Returns whether this scope loads rules on clients.
     *
     * @return true if this scope is loaded on clients
     */
    boolean isLoadedOnClient();

    /**
     * Returns whether this scope loads rules on servers.
     *
     * @return true if this scope is loaded on servers
     */
    boolean isLoadedOnServer();

    /**
     * Creates an integer value backed by this scope.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to an integer
     */
    default <TContext> ShogiValue<TContext, Integer> intValue(ResourceLocation identifier, Function<TContext, Integer> defaultValue) {
        return Shogi.__factories().value(identifier, this, defaultValue).coerce(Coercion.INT);
    }

    /**
     * Creates a float value backed by this scope.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a float
     */
    default <TContext> ShogiValue<TContext, Float> floatValue(ResourceLocation identifier, Function<TContext, Float> defaultValue) {
        return Shogi.__factories().value(identifier, this, defaultValue).coerce(Coercion.FLOAT);
    }

    /**
     * Creates a boolean value backed by this scope.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a boolean
     */
    default <TContext> ShogiValue<TContext, Boolean> booleanValue(ResourceLocation identifier, Function<TContext, Boolean> defaultValue) {
        return Shogi.__factories().value(identifier, this, defaultValue).coerce(Coercion.BOOLEAN);
    }

    /**
     * Creates a string value backed by this scope.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a string
     */
    default <TContext> ShogiValue<TContext, String> stringValue(ResourceLocation identifier, Function<TContext, String> defaultValue) {
        return Shogi.__factories().value(identifier, this, defaultValue).coerce(Coercion.STRING);
    }

    /**
     * Creates a component value backed by this scope.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a chat component
     */
    default <TContext> ShogiValue<TContext, Component> componentValue(ResourceLocation identifier, Function<TContext, Component> defaultValue) {
        return Shogi.__factories().value(identifier, this, defaultValue).coerce(Coercion.COMPONENT);
    }

    /**
     * Creates a value whose default provider already returns an Either payload.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback resolver returning either success or failure
     * @param <TContext> the context type supplied during resolution
     * @param <TSuccess> the success value type
     * @return a value that resolves to the given either payload shape
     */
    default <TContext, TSuccess> ShogiValue<TContext, ?> maybe(ResourceLocation identifier, Function<TContext, Either<TSuccess, ?>> defaultValue) {
        return Shogi.__factories().maybe(identifier, this, defaultValue);
    }

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
    <TContext, TSuccess> Either<?, ?> resolve(ResourceLocation identifier, TContext context, Function<TContext, Either<TSuccess, ?>> defaultProvider);

    /**
     * Registers an override provider consulted during resolution.
     *
     * @param provider override provider
     */
    void registerOverrideProvider(ShogiOverrideProvider provider);

    /**
     * Resolves the current override effect for the given value identifier.
     *
     * @param identifier value identifier
     * @return override effect, or empty when none applies
     */
    Optional<ShogiEffect<?>> getOverride(ResourceLocation identifier);

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
