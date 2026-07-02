package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public abstract class AbstractEffectExecutor implements EffectExecutor {

    protected final Map<AggregateKey<?>, Object> aggregates = new HashMap<>();
    private final Map<ResourceLocation, BiFunction<?, ?, ?>> aggregateOverrides = new HashMap<>();
    private final Map<ResourceLocation, BiConsumer<?, ?>> consumeOverrides = new HashMap<>();
    private final Map<ResourceLocation, Consumer<Runnable>> executeOverrides = new HashMap<>();

    public AbstractEffectExecutor() {
        this(null);
    }

    public AbstractEffectExecutor(@Nullable AbstractEffectExecutor copy) {
        if (copy != null) {
            aggregateOverrides.putAll(copy.aggregateOverrides);
            consumeOverrides.putAll(copy.consumeOverrides);
            executeOverrides.putAll(copy.executeOverrides);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T aggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, T> aggregator) {
        var aggregate = (T) aggregates.get(key);
        if (aggregate == null) {
            aggregate = initializer.get();
        }
        T newAggregate = applyAggregateOverride(key, aggregator, aggregate);
        aggregates.put(key, newAggregate);
        return newAggregate;
    }

    @Override
    public <T, R> void overrideAggregate(ResourceLocation identifier, BiFunction<Function<T, R>, T, R> override) {
        aggregateOverrides.put(identifier, override);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R statefulAggregate(AggregateKey<T> key, Supplier<T> initializer, Function<T, R> aggregator) {
        var aggregate = (T) aggregates.get(key);
        if (aggregate == null) {
            aggregate = initializer.get();
            aggregates.put(key, aggregate);
        }
        return applyAggregateOverride(key, aggregator, aggregate);
    }

    @Override
    public <T> void overrideConsume(ResourceLocation identifier, BiConsumer<Consumer<T>, T> override) {
        consumeOverrides.put(identifier, override);
    }

    @Override
    public void overrideExecute(ResourceLocation identifier, Consumer<Runnable> override) {
        executeOverrides.put(identifier, override);
    }

    @SuppressWarnings("unchecked")
    protected <T, R> R applyAggregateOverride(AggregateKey<T> key, Function<T, R> operation, T value) {
        final var override = (BiFunction<Function<T, R>, T, R>) aggregateOverrides.get(key.identifier());
        if (override != null) {
            return override.apply(operation, value);
        } else {
            return operation.apply(value);
        }
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

    protected void applyExecuteOverride(ResourceLocation identifier, Runnable runnable) {
        final var override = executeOverrides.get(identifier);
        if (override != null) {
            override.accept(runnable);
        } else {
            runnable.run();
        }
    }
}
