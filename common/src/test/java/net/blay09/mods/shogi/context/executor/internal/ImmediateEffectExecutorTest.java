package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ImmediateEffectExecutorTest {

    @Test
    void executeUsesOverrideExecute() {
        final var executor = new ImmediateEffectExecutor();
        final var identifier = ResourceLocation.fromNamespaceAndPath("shogi", "test_execute");
        final var didRun = new AtomicBoolean(false);

        executor.overrideExecute(identifier, ignored -> {
        });
        executor.execute(identifier, () -> didRun.set(true));

        assertFalse(didRun.get());
    }

    @Test
    void consumeUsesOverrideConsume() {
        final var executor = new ImmediateEffectExecutor();
        final var aggregateKey = AggregateKey.<Integer>of(ResourceLocation.fromNamespaceAndPath("shogi", "test_consume"));
        final var consumed = new AtomicInteger(0);

        executor.aggregate(aggregateKey, () -> 0, it -> 4);
        executor.overrideConsume(aggregateKey.identifier(), (Consumer<Integer> operation, Integer value) -> operation.accept(value + 3));
        executor.consume(aggregateKey, consumed::set);

        assertEquals(7, consumed.get());
    }
}
