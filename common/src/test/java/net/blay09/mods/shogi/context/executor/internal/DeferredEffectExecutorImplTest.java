package net.blay09.mods.shogi.context.executor.internal;

import net.blay09.mods.shogi.context.executor.aggregate.AggregateKey;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeferredEffectExecutorImplTest {

    @Test
    void executeDoesNotRecurseWhenRunnableCallsExecuteAgain() {
        final var executor = new DeferredEffectExecutorImpl();
        final var didRun = new AtomicBoolean(false);
        executor.execute(Identifier.fromNamespaceAndPath("shogi", "reentrant"), () -> {
            didRun.set(true);
            executor.execute();
        });

        assertDoesNotThrow(() -> executor.execute());
        assertTrue(didRun.get());
    }

    @Test
    void executeUsesOverrideExecute() {
        final var executor = new DeferredEffectExecutorImpl();
        final var identifier = Identifier.fromNamespaceAndPath("shogi", "test_execute");
        final var didRun = new AtomicBoolean(false);

        executor.execute(identifier, () -> didRun.set(true));
        executor.overrideExecute(identifier, _ -> {
        });
        executor.execute();

        assertFalse(didRun.get());
    }

    @Test
    void executeUsesOverrideConsume() {
        final var executor = new DeferredEffectExecutorImpl();
        final var aggregateKey = AggregateKey.<Integer>of(Identifier.fromNamespaceAndPath("shogi", "test_consume"));
        final var consumed = new AtomicInteger(0);

        executor.aggregate(aggregateKey, () -> 0, it -> 3);
        executor.consume(aggregateKey, consumed::set);
        executor.overrideConsume(aggregateKey.identifier(), (Consumer<Integer> operation, Integer value) -> operation.accept(value * 2));
        executor.execute();

        assertEquals(6, consumed.get());
    }

    @Test
    void latestOverrideWins() {
        final var executor = new DeferredEffectExecutorImpl();
        final var identifier = Identifier.fromNamespaceAndPath("shogi", "test_latest");
        final var runs = new AtomicInteger(0);

        executor.execute(identifier, runs::incrementAndGet);
        executor.overrideExecute(identifier, runnable -> {
        });
        executor.overrideExecute(identifier, Runnable::run);
        executor.execute();

        assertEquals(1, runs.get());
    }
}
