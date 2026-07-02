package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.coercion.Coercion;
import net.minecraft.resources.ResourceLocation;

public record BinaryOpEffect(String op, ShogiEffect<?> left, ShogiEffect<?> right) implements ShogiEffect<Object> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "binary_op");

    public static MapCodec<BinaryOpEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf("op").forGetter(BinaryOpEffect::op),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("left").forGetter(BinaryOpEffect::left),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("right").forGetter(BinaryOpEffect::right)
        ).apply(builder, BinaryOpEffect::new));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        final var leftResult = left.apply(context);
        final var leftFailure = leftResult.right().orElse(null);
        if (leftFailure != null) {
            return Either.right(leftFailure);
        }

        final var rightResult = right.apply(context);
        final var rightFailure = rightResult.right().orElse(null);
        if (rightFailure != null) {
            return Either.right(rightFailure);
        }

        final var leftValue = leftResult.mapLeft(Coercion.FLOAT).orThrow();
        final var rightValue = rightResult.mapLeft(Coercion.FLOAT).orThrow();

        return switch (op) {
            case "+" -> Either.left(leftValue + rightValue);
            case "-" -> Either.left(leftValue - rightValue);
            case "*" -> Either.left(leftValue * rightValue);
            case "/" -> {
                if (rightValue == 0f) {
                    yield Either.right(new ArithmeticException("Division by zero"));
                }
                yield Either.left(leftValue / rightValue);
            }
            default -> Either.right(new IllegalArgumentException("Unsupported binary operator: " + op));
        };
    }
}
