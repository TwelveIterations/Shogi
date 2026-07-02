package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.internal.MinimalShogiFactories;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@SuppressWarnings("unused") // loaded via Reflection
public class ShogiFactoriesImpl extends MinimalShogiFactories {

    @Override
    public ShogiScope scope(ResourceLocation identifier) {
        return ShogiScopeRegistry.getOrCreate(identifier, (id) -> ShogiDefaults.registerDefaults(new ShogiScopeImpl(id)));
    }

    @Override
    public ShogiScope scope(ResourceLocation identifier, Consumer<ShogiScope> configure) {
        final var scope = ShogiScopeRegistry.getOrCreate(identifier, (id) -> ShogiDefaults.registerDefaults(new ShogiScopeImpl(id)));
        configure.accept(scope);
        return scope;
    }
}
