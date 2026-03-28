package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AndEffect(List<ShogiEffect<?>> conditions) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "and");

    public static MapCodec<AndEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                scope.getEffectCodec().listOf(1, Integer.MAX_VALUE).fieldOf("conditions").forGetter(AndEffect::conditions)
        ).apply(builder, AndEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<Boolean, Object> apply(ShogiContext context) {
        for (final var condition : conditions) {
            if (!condition.test(context)) {
                return Either.left(false);
            }
        }
        return Either.left(true);
    }
}
