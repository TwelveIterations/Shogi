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

public record ClampMinEffect(ShogiEffect<?> value, ShogiEffect<?> min) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "clamp_min");

    public static MapCodec<ClampMinEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("value").forGetter(ClampMinEffect::value),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("min").forGetter(ClampMinEffect::min)
        ).apply(builder, ClampMinEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public List<ShogiEffect<?>> nestedEffects() {
        return List.of(value, min);
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

        final var valueNumber = valueResult.mapLeft(Coercion.FLOAT).orThrow();
        final var minNumber = minResult.mapLeft(Coercion.FLOAT).orThrow();
        return Either.left(Math.max(valueNumber, minNumber));
    }
}
