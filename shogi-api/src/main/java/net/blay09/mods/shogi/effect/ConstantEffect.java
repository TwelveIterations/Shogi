package net.blay09.mods.shogi.effect;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

/**
 * Effect that always succeeds with a constant JSON value.
 *
 * @param value constant payload returned on success
 */
public record ConstantEffect(JsonElement value) implements ShogiEffect<Object> {

    /**
     * Identifier used for the constant effect type.
     */
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "constant");
    /**
     * Codec for serializing/deserializing constant effects.
     */
    public static final MapCodec<ConstantEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ExtraCodecs.JSON.fieldOf("value").forGetter(ConstantEffect::value)
    ).apply(builder, ConstantEffect::new));

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    /**
     * Returns the constant JSON payload as a successful result.
     *
     * @param context evaluation context
     * @return left either containing {@link #value()}
     */
    @Override
    public Either<?, ?> apply(ShogiContext context) {
        return Either.left(value);
    }
}
