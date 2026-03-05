package net.blay09.mods.shogi.common.context.aggregate;

import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.executor.DeferredEffectExecutor;
import net.blay09.mods.shogi.context.executor.internal.DeferredEffectExecutorImpl;
import net.blay09.mods.shogi.context.internal.ShogiContextImpl;
import org.jspecify.annotations.Nullable;

public class ShogiAggregateContext extends ShogiContextImpl {

    public ShogiAggregateContext(@Nullable ShogiContext parent) {
        super(parent, parent != null && parent.executor() instanceof DeferredEffectExecutor deferredEffectExecutor ? deferredEffectExecutor : new DeferredEffectExecutorImpl());
    }

    @Override
    public MutableShogiContext fork() {
        return new ShogiAggregateContext(this);
    }
}
