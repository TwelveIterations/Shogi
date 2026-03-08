package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class ImmediateEffectExecutor extends AbstractEffectExecutor {
    @Override
    @SuppressWarnings("unchecked")
    public <T> void consume(AggregateKey<T> key, Consumer<T> o) {
        final var aggregate = aggregates.get(key);
        if (aggregate != null) {
            applyConsumeOverride(key, o, (T) aggregate);
        }
    }

    @Override
    public void execute(Identifier identifier, Runnable runnable) {
        applyExecuteOverride(identifier, runnable);
    }
}
