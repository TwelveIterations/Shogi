package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

public record ConditionEffect(
        ShogiEffect<?> condition,
        ShogiEffect<?> trueEffect,
        ShogiEffect<?> falseEffect
) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "condition");

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    public static MapCodec<ConditionEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("condition").forGetter(ConditionEffect::condition),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("then").forGetter(ConditionEffect::trueEffect),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("else").orElse(EmptyEffect.INSTANCE).forGetter(ConditionEffect::falseEffect)
        ).apply(builder, ConditionEffect::new));
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        return condition.test(context) ? trueEffect.apply(context) : falseEffect.apply(context);
    }
}
