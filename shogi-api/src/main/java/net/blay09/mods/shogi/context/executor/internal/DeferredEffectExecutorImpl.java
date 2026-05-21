package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.DeferredEffectExecutor;
import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DeferredEffectExecutorImpl extends AbstractEffectExecutor implements DeferredEffectExecutor {
    private final Map<AggregateKey<?>, Consumer<?>> consumers = new HashMap<>();
    private final Map<Identifier, Runnable> runnables = new HashMap<>();
    private boolean executing;

    public DeferredEffectExecutorImpl() {
    }

    public DeferredEffectExecutorImpl(@Nullable AbstractEffectExecutor copy) {
        super(copy);
    }

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
        applyConsumeOverride((AggregateKey<T>) key, consumer, (T) aggregates.get(key));
    }

    private void executeRunnable(Identifier identifier, Runnable runnable) {
        applyExecuteOverride(identifier, runnable);
    }

    @Override
    public void execute() {
        if (executing) {
            return;
        }

        executing = true;
        try {
            consumers.forEach(this::executeConsumer);
            runnables.forEach(this::executeRunnable);
        } finally {
            executing = false;
        }
    }
}
