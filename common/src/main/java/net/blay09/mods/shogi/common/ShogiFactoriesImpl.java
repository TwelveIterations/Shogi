package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.internal.MinimalShogiFactories;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused") // loaded via Reflection
public class ShogiFactoriesImpl extends MinimalShogiFactories {

    @Override
    public ShogiScope scope(Identifier identifier) {
        return ShogiScopeRegistry.getOrCreate(identifier, (id) -> ShogiDefaults.registerDefaults(new ShogiScopeImpl(id)));
    }

    @Override
    public ShogiScope scope(Identifier identifier, Consumer<ShogiScope> configure) {
        return ShogiScopeRegistry.getOrCreate(identifier, (id) -> {
            final var scope = ShogiDefaults.registerDefaults(new ShogiScopeImpl(id));
            configure.accept(scope);
            return scope;
        });
    }
}
