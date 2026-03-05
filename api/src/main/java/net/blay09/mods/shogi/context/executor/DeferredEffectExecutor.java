package net.blay09.mods.shogi.context.executor;

/**
 * Defers execution of effects until {@link #execute()} is called.
 */
public interface DeferredEffectExecutor extends EffectExecutor {
    /**
     * Executes all scheduled effects.
     */
    void execute();
}
