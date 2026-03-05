package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.blay09.mods.shogi.context.executor.EffectExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractEffectExecutor implements EffectExecutor {

    protected final Map<AggregateKey<?>, Object> aggregates = new HashMap<>();

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
}
