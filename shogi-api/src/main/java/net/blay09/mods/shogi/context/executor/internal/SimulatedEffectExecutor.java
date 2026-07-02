package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SimulatedEffectExecutor extends AbstractEffectExecutor {
    @Override
    public <T> void consume(AggregateKey<T> key, Consumer<T> o) {
    }

    @Override
    public void execute(ResourceLocation identifier, Runnable runnable) {
    }
}
