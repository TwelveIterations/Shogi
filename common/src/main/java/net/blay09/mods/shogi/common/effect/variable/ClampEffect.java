package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.minecraft.resources.Identifier;

public record ClampEffect(ShogiEffect<?> value, ShogiEffect<?> min, ShogiEffect<?> max) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "clamp");

    public static MapCodec<ClampEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("value").forGetter(ClampEffect::value),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("min").forGetter(ClampEffect::min),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("max").forGetter(ClampEffect::max)
        ).apply(builder, ClampEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        final var valueResult = value.apply(context);
        final var valueFailure = valueResult.right().orElse(null);
        if (valueFailure != null) {
            return Either.right(valueFailure);
        }

        final var minResult = min.apply(context);
        final var minFailure = minResult.right().orElse(null);
        if (minFailure != null) {
            return Either.right(minFailure);
        }

        final var maxResult = max.apply(context);
        final var maxFailure = maxResult.right().orElse(null);
        if (maxFailure != null) {
            return Either.right(maxFailure);
        }

        final var valueNumber = valueResult.mapLeft(Coercion.FLOAT).orThrow();
        final var minNumber = minResult.mapLeft(Coercion.FLOAT).orThrow();
        final var maxNumber = maxResult.mapLeft(Coercion.FLOAT).orThrow();
        return Either.left(Math.max(minNumber, Math.min(valueNumber, maxNumber)));
    }
}
