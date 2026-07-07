package net.blay09.mods.shogi.common.effect.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;

public record VariableEffect(String name) implements ShogiEffect<Object> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "variable");
    public static final MapCodec<VariableEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(VariableEffect::name)
    ).apply(builder, VariableEffect::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<?, ?> apply(ShogiContext context) {
        final var value = context.getVariable(name);
        if (value.isEmpty()) {
            return Either.right(new MissingVariableFailure(name));
        }

        final var resolvedValue = value.get();
        if (resolvedValue instanceof ShogiEffect<?> effect) {
            return effect.apply(context);
        }
        return Either.left(resolvedValue);
    }
}
