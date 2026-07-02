package net.blay09.mods.shogi.internal;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.ShogiFactories;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

public class MinimalShogiFactories implements ShogiFactories {

    @Override
    public ShogiScope scope(ResourceLocation identifier) {
        return ShogiScopeRegistry.getOrCreate(identifier, ShogiScopeImpl::new);
    }

    @Override
    public ShogiScope scope(ResourceLocation identifier, Consumer<ShogiScope> configure) {
        final var scope = ShogiScopeRegistry.getOrCreate(identifier, ShogiScopeImpl::new);
        configure.accept(scope);
        return scope;
    }

    @Override
    public <TContext, TSuccess> ShogiValue<TContext, ?> value(ResourceLocation identifier, ShogiScope scope, Function<TContext, TSuccess> defaultProvider) {
        return new ShogiValueImpl<>(identifier, scope, context -> scope.resolve(identifier, context, (it) -> Either.left(defaultProvider.apply(it))));
    }

    @Override
    public <TContext, TSuccess> ShogiValue<TContext, ?> maybe(ResourceLocation identifier, ShogiScope scope, Function<TContext, Either<TSuccess, ?>> defaultRule) {
        return new ShogiValueImpl<>(identifier, scope, context -> scope.resolve(identifier, context, defaultRule));
    }

}
