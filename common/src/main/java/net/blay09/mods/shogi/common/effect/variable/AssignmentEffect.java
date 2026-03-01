package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

public record AssignmentEffect(String variable, ShogiEffect<?> value) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "assignment");

    public static MapCodec<AssignmentEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf("variable").forGetter(AssignmentEffect::variable),
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("value").forGetter(AssignmentEffect::value)
        ).apply(builder, AssignmentEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        return value.apply(context).ifLeft(it -> context.withVariable(variable, it));
    }
}
