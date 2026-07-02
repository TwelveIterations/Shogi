package net.blay09.mods.shogi.internal;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

public class ShogiValueImpl<TContext, TSuccess> implements ShogiValue<TContext, TSuccess> {

    private final ResourceLocation identifier;
    private final ShogiScope scope;
    private final Function<TContext, Either<TSuccess, ?>> resolver;

    public ShogiValueImpl(ResourceLocation identifier, ShogiScope scope, Function<TContext, Either<TSuccess, ?>> resolver) {
        this.identifier = identifier;
        this.scope = scope;
        this.resolver = resolver;
    }

    @Override
    public TSuccess getOrThrow(TContext context) {
        return get(context).orThrow();
    }

    @Override
    public TSuccess getOrDefault(TContext context) {
        return get(context).left().orElseThrow(() -> new IllegalStateException("Failed to evaluate value for " + identifier));
    }

    @Override
    public TSuccess getOrElse(TContext context, TSuccess fallback) {
        return get(context).left().orElse(fallback);
    }

    @Override
    public TSuccess getOrElseGet(TContext context, Supplier<TSuccess> fallbackSupplier) {
        return get(context).left().orElseGet(fallbackSupplier);
    }

    @Override
    public Either<TSuccess, ?> get(TContext context) {
        return resolver.apply(context);
    }

    @Override
    public <TResult> ShogiValue<TContext, TResult> coerce(Function<Object, TResult> coercion) {
        return new ShogiValueImpl<>(identifier, scope, context -> resolver.apply(context)
                .mapLeft(coercion));
    }

    @Override
    public <TResult> ShogiValue<TContext, TResult> require(Class<TResult> resultType) {
        return new ShogiValueImpl<>(identifier, scope, context -> resolver.apply(context)
                .mapLeft(it -> Coercion.toClass(resultType).apply(it)));
    }

    @Override
    public ShogiValue<TContext, TSuccess> networked() {
        scope.getNetworkCache().addNetworkedValue(identifier);
        return this;
    }
}
