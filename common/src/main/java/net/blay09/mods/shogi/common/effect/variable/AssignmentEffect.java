package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.MissingContextException;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

import java.util.List;

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
    public List<ShogiEffect<?>> nestedEffects() {
        return List.of(value);
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        if (!(context instanceof MutableShogiContext mutableContext)) {
            return Either.right(new MissingContextException(context));
        }

        return value.apply(context).ifLeft(it -> mutableContext.withVariable(variable, it));
    }
}
