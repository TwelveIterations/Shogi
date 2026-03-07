package net.blay09.mods.shogi.internal;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.ShogiFactories;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class MinimalShogiFactories implements ShogiFactories {

    @Override
    public ShogiScope scope(Identifier identifier) {
        return ShogiScopeRegistry.getOrCreate(identifier, ShogiScopeImpl::new);
    }

    @Override
    public ShogiScope scope(Identifier identifier, Consumer<ShogiScope> configure) {
        return ShogiScopeRegistry.getOrCreate(identifier, id -> {
            final var scope = new ShogiScopeImpl(id);
            configure.accept(scope);
            return scope;
        });
    }

    @Override
    public <TContext, TSuccess> ShogiValue<TContext, ?> value(Identifier identifier, ShogiScope scope, Function<TContext, TSuccess> defaultProvider) {
        return new ShogiValueImpl<>(identifier, scope, context -> scope.resolve(identifier, context, defaultProvider));
    }

    @Override
    public <TContext, TSuccess, TFailure> ShogiValue<TContext, ?> maybe(Identifier identifier, ShogiScope scope, Function<TContext, Either<TSuccess, TFailure>> defaultRule) {
        return new ShogiValueImpl<>(identifier, scope, context -> scope.resolve(identifier, context, defaultRule));
    }

}
