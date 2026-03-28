package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

public record IsBrighterThan(ShogiEffect<?> lightLevel) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_brighter_than");

    public static MapCodec<IsBrighterThan> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("light_level").forGetter(IsBrighterThan::lightLevel)
        ).apply(instance, IsBrighterThan::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var lightLevelResult = lightLevel.apply(context);
        final var failure = lightLevelResult.right().orElse(null);
        if (failure != null) {
            return Either.right(failure);
        }

        final var level = context.requireLevel();
        final var blockPos = context.requireBlockPos();
        final var currentLightLevel = level.getMaxLocalRawBrightness(blockPos);
        final var expectedLightLevel = lightLevelResult.mapLeft(Coercion.FLOAT).orThrow();
        return Either.left(currentLightLevel > expectedLightLevel);
    }
}
