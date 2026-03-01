package net.blay09.mods.shogi.common;

import net.blay09.mods.shogi.internal.MinimalShogiFactories;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused") // loaded via Reflection
public class ShogiFactoriesImpl extends MinimalShogiFactories {

    @Override
    public ShogiScope scope(Identifier identifier) {
        return ShogiScopeRegistry.getOrCreate(identifier, (id) -> ShogiDefaults.registerDefaults(new ShogiScopeImpl(id)));
    }

}
