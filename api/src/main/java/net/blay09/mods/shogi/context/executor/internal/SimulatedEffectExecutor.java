package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class SimulatedEffectExecutor extends AbstractEffectExecutor {
    @Override
    public <T> void consume(AggregateKey<T> key, Consumer<T> o) {
    }

    @Override
    public void execute(Identifier identifier, Runnable runnable) {
    }
}
