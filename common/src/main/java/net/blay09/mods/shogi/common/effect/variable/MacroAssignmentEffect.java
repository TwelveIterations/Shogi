package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.MissingContextException;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;

public record MacroAssignmentEffect(String variable, ShogiEffect<?> value) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "macro_assignment");

    public static MapCodec<MacroAssignmentEffect> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf("variable").forGetter(MacroAssignmentEffect::variable),
                scope.getEffectCodec().fieldOf("value").forGetter(MacroAssignmentEffect::value)
        ).apply(builder, MacroAssignmentEffect::new));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        if (!(context instanceof MutableShogiContext mutableContext)) {
            return Either.right(new MissingContextException(context));
        }

        mutableContext.withVariable(variable, value);
        return Either.left(value);
    }
}
