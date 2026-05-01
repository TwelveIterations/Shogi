package net.blay09.mods.shogi.context.executor;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.context.executor.internal.DeferredEffectExecutorImpl;
import net.blay09.mods.shogi.context.executor.internal.ImmediateEffectExecutor;
import net.blay09.mods.shogi.context.executor.internal.SimulatedEffectExecutor;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Executes aggregate operations for context-aware effects.
 */
public interface EffectExecutor {
    /**
     * Updates and returns an aggregate value for the given key.
     *
     * @param key aggregate key
     * @param initializer initializer used when the key has no value yet
     * @param aggregator function used to update the current value
     * @param <T> aggregate value type
     * @return the updated aggregate value
     */
    <T> T aggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, T> aggregator);

    /**
     * Updates an aggregate value while returning a custom operation result.
     *
     * @param key aggregate key
     * @param initializer initializer used when the key has no value yet
     * @param aggregator function that modifies the aggregate state and returns a step-scoped result
     * @param <T> aggregate value type
     * @param <R> step return value type
     * @return the step-scoped result value returned by {@code aggregator}
     */
    <T, R> R statefulAggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, R> aggregator);

    /**
     * Runs a consumer for the aggregate value of the given key, if present.
     *
     * @param key aggregate key
     * @param consumer consumer invoked with the current aggregate value
     * @param <T> aggregate value type
     */
    <T> void consume(AggregateKey<T> key, Consumer<T> consumer);

    /**
     * Overrides how aggregate consume operations are executed for the given identifier.
     *
     * @param identifier effect identifier
     * @param override override callback receiving the original operation and aggregate value
     * @param <T> aggregate value type
     */
    default <T> void overrideConsume(Identifier identifier, BiConsumer<Consumer<T>, T> override) {
    }

    /**
     * Schedules a side effect to run on success.
     *
     * @param identifier side effect identifier
     * @param runnable runnable invoked if the evaluation succeeded
     */
    void execute(Identifier identifier, Runnable runnable);

    /**
     * Overrides how execute operations are executed for the given identifier.
     *
     * @param identifier effect identifier
     * @param override override callback receiving the original runnable
     */
    default void overrideExecute(Identifier identifier, Consumer<Runnable> override) {
    }

    static DeferredEffectExecutor deferred() {
        return new DeferredEffectExecutorImpl();
    }

    static EffectExecutor immediate() {
        return new ImmediateEffectExecutor();
    }

    static EffectExecutor simulated() {
        return new SimulatedEffectExecutor();
    }
}
