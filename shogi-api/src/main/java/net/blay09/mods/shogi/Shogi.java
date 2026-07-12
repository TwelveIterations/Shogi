package net.blay09.mods.shogi;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Main entry point for creating and resolving Shogi-backed values.
 */
public class Shogi {

    private static final ShogiFactories factories = ShogiFactories.create();
    private static final ShogiScope defaultScope = factories.scope(Identifier.fromNamespaceAndPath("shogi", "rules"));

    /**
     * Returns the default global scope used by the convenience factory methods in this class.
     *
     * @return the shared default Shogi scope
     */
    public static ShogiScope defaultScope() {
        return defaultScope;
    }

    /**
     * Provides access to the internal factories providing the Shogi implementation.
     * Internal use only. Use methods in {@link Shogi} or {@link ShogiScope} directly instead.
     * @return internal factories
     */
    public static ShogiFactories __factories() {
        return factories;
    }

    /**
     * Returns a scope for the given identifier, creating it if needed.
     *
     * @param identifier scope identifier
     * @return scope instance
     */
    public static ShogiScope scope(Identifier identifier) {
        return factories.scope(identifier);
    }

    /**
     * Returns a scope for the given identifier, creating it if needed, and applies the configure callback.
     *
     * @param identifier scope identifier
     * @return scope instance
     */
    public static ShogiScope scope(Identifier identifier, Consumer<ShogiScope> configure) {
        return factories.scope(identifier, configure);
    }

    /**
     * Creates an integer value backed by the default scope.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to an integer
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext> ShogiValue<TContext, Integer> intValue(Identifier identifier, Function<TContext, Integer> defaultValue) {
        return factories.value(identifier, defaultScope(), defaultValue).coerce(Coercion.INT);
    }

    /**
     * Creates a float value backed by the default scope.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a float
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext> ShogiValue<TContext, Float> floatValue(Identifier identifier, Function<TContext, Float> defaultValue) {
        return factories.value(identifier, defaultScope(), defaultValue).coerce(Coercion.FLOAT);
    }

    /**
     * Creates a boolean value backed by the default scope.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a boolean
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext> ShogiValue<TContext, Boolean> booleanValue(Identifier identifier, Function<TContext, Boolean> defaultValue) {
        return factories.value(identifier, defaultScope(), defaultValue).coerce(Coercion.BOOLEAN);
    }

    /**
     * Creates a string value backed by the default scope.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a string
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext> ShogiValue<TContext, String> stringValue(Identifier identifier, Function<TContext, String> defaultValue) {
        return factories.value(identifier, defaultScope(), defaultValue).coerce(Coercion.STRING);
    }

    /**
     * Creates a component value backed by the default scope.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback value provider used when no override applies
     * @param <TContext> the context type supplied during resolution
     * @return a value that resolves to a chat component
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext> ShogiValue<TContext, Component> componentValue(Identifier identifier, Function<TContext, Component> defaultValue) {
        return factories.value(identifier, defaultScope(), defaultValue).coerce(Coercion.COMPONENT);
    }

    /**
     * Creates a value whose default provider already returns an Either payload.
     *
     * @deprecated Use {@link #scope(Identifier)} and create the value on the returned {@link ShogiScope} instead.
     *
     * @param identifier the unique identifier for this value
     * @param defaultValue the fallback resolver returning either success or failure
     * @param <TContext> the context type supplied during resolution
     * @param <TSuccess> the success value type
     * @return a value that resolves to the given either payload shape
     */
    @Deprecated(since = "26.3.0.0")
    public static <TContext, TSuccess> ShogiValue<TContext, ?> maybe(Identifier identifier, Function<TContext, Either<TSuccess, ?>> defaultValue) {
        return factories.maybe(identifier, defaultScope(), defaultValue);
    }

}
