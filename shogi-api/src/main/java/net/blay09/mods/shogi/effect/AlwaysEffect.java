package net.blay09.mods.shogi.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;

/**
 * Effect that always resolves successfully to {@code true}.
 */
public class AlwaysEffect implements ShogiEffect<Boolean> {

    /**
     * Shared singleton instance.
     */
    public static final AlwaysEffect INSTANCE = new AlwaysEffect();
    /**
     * Identifier used for the always-true effect type.
     */
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "always");
    /**
     * Codec for serializing/deserializing always-true effects.
     */
    public static final MapCodec<AlwaysEffect> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<Boolean, ?> apply(ShogiContext context) {
        return Either.left(true);
    }

    @Override
    public String toString() {
        return "AlwaysEffect";
    }
}
