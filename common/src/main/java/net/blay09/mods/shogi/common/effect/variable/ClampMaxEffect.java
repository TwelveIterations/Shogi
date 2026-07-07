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

import java.util.List;

public record ClampMaxEffect(ShogiEffect<?> value, ShogiEffect<?> max) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "clamp_max");

    public static MapCodec<ClampMaxEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("value").forGetter(ClampMaxEffect::value),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("max").forGetter(ClampMaxEffect::max)
        ).apply(builder, ClampMaxEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public List<ShogiEffect<?>> nestedEffects() {
        return List.of(value, max);
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        final var valueResult = value.apply(context);
        final var valueFailure = valueResult.right().orElse(null);
        if (valueFailure != null) {
            return Either.right(valueFailure);
        }

        final var maxResult = max.apply(context);
        final var maxFailure = maxResult.right().orElse(null);
        if (maxFailure != null) {
            return Either.right(maxFailure);
        }

        final var valueNumber = valueResult.mapLeft(Coercion.FLOAT).orThrow();
        final var maxNumber = maxResult.mapLeft(Coercion.FLOAT).orThrow();
        return Either.left(Math.min(valueNumber, maxNumber));
    }
}
