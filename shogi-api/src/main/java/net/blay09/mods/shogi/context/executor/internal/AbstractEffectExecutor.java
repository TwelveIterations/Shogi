package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractEffectExecutor implements EffectExecutor {

    protected final Map<AggregateKey<?>, Object> aggregates = new HashMap<>();
    private final Map<Identifier, BiConsumer<?, ?>> consumeOverrides = new HashMap<>();
    private final Map<Identifier, Consumer<Runnable>> executeOverrides = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T aggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, T> aggregator) {
        var aggregate = (T) aggregates.get(key);
        if (aggregate == null) {
            aggregate = initializer.get();
        }
        T newAggregate = aggregator.apply(aggregate);
        aggregates.put(key, newAggregate);
        return newAggregate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R statefulAggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, R> aggregator) {
        var aggregate = (T) aggregates.get(key);
        if (aggregate == null) {
            aggregate = initializer.get();
            aggregates.put(key, aggregate);
        }
        return aggregator.apply(aggregate);
    }

    @Override
    public <T> void overrideConsume(Identifier identifier, BiConsumer<Consumer<T>, T> override) {
        consumeOverrides.put(identifier, override);
    }

    @Override
    public void overrideExecute(Identifier identifier, Consumer<Runnable> override) {
        executeOverrides.put(identifier, override);
    }

    @SuppressWarnings("unchecked")
    protected <T> void applyConsumeOverride(AggregateKey<T> key, Consumer<T> operation, T value) {
        final var override = (BiConsumer<Consumer<T>, T>) consumeOverrides.get(key.identifier());
        if (override != null) {
            override.accept(operation, value);
        } else {
            operation.accept(value);
        }
    }

    protected void applyExecuteOverride(Identifier identifier, Runnable runnable) {
        final var override = executeOverrides.get(identifier);
        if (override != null) {
            override.accept(runnable);
        } else {
            runnable.run();
        }
    }
}
