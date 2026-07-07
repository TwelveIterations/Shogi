package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

public record HasValueEffect(String variable) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "has_value");
    public static final MapCodec<HasValueEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("variable").forGetter(HasValueEffect::variable)
    ).apply(builder, HasValueEffect::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        return Either.left(context.getVariable(variable).isPresent());
    }
}
