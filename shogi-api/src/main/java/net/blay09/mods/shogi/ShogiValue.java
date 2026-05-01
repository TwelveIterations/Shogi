package net.blay09.mods.shogi;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.coercion.Coercion;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handle for resolving a configured Shogi value for a given context.
 *
 * @param <TContext> the context type used during resolution
 * @param <TSuccess> the success value type
 */
public interface ShogiValue<TContext, TSuccess> {
    /**
     * Resolves this value and throws if resolution produces a failure.
     *
     * @param context the resolution context
     * @return the resolved success value
     */
    TSuccess getOrThrow(TContext context);

    /**
     * Resolves this value and returns the default/fallback success value on failure.
     *
     * @param context the resolution context
     * @return the resolved value or the configured default
     */
    TSuccess getOrDefault(TContext context);

    /**
     * Resolves this value and returns the provided fallback on failure.
     *
     * @param context the resolution context
     * @param fallback the fallback value to return on failure
     * @return the resolved value or {@code fallback}
     */
    TSuccess getOrElse(TContext context, TSuccess fallback);

    /**
     * Resolves this value and lazily obtains a fallback on failure.
     *
     * @param context the resolution context
     * @param fallback the fallback supplier used on failure
     * @return the resolved value or the supplied fallback
     */
    TSuccess getOrElseGet(TContext context, Supplier<TSuccess> fallback);

    /**
     * Resolves this value and returns the raw either payload.
     *
     * @param context the resolution context
     * @return either a success value or a failure payload
     */
    Either<TSuccess, ?> get(TContext context);

    /**
     * Creates a derived value that coerces the resolved success value through the provided function.
     *
     * @param coercion coercion function applied to success values
     * @param <TResult> the target success type
     * @return a derived value with coerced success type
     */
    <TResult> ShogiValue<TContext, TResult> coerce(Function<Object, TResult> coercion);

    /**
     * Creates a derived value that requires the resolved success value to be assignable to {@code resultType}.
     *
     * @param resultType required runtime class of the resolved value
     * @param <TResult> the target success type
     * @return a derived value enforcing the requested runtime type
     * @see Coercion#toClass(Class)
     */
    <TResult> ShogiValue<TContext, TResult> require(Class<TResult> resultType);

    /**
     * Marks this value as network-synchronized when supported by the active runtime.
     *
     * @return this value instance for chaining
     */
    ShogiValue<TContext, TSuccess> networked();

}
