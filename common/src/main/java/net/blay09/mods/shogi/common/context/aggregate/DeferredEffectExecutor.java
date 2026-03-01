package net.blay09.mods.shogi.common.context.aggregate;

import net.blay09.mods.shogi.context.aggregate.AggregateKey;
import net.blay09.mods.shogi.context.aggregate.internal.AbstractEffectExecutor;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DeferredEffectExecutor extends AbstractEffectExecutor {
    private final Map<AggregateKey<?>, Consumer<?>> consumers = new HashMap<>();
    private final Map<Identifier, Runnable> runnables = new HashMap<>();

    @Override
    public <T> void consume(AggregateKey<T> key, Consumer<T> o) {
        consumers.put(key, o);
    }

    @Override
    public void execute(Identifier identifier, Runnable runnable) {
        runnables.put(identifier, runnable);
    }

    @SuppressWarnings("unchecked")
    private <T> void executeConsumer(AggregateKey<?> key, Consumer<T> consumer) {
        consumer.accept((T) aggregates.get(key));
    }

    public void execute() {
        consumers.forEach(this::executeConsumer);
        runnables.values().forEach(Runnable::run);
    }
}
