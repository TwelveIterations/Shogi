package net.blay09.mods.shogi.context.executor.internal;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
