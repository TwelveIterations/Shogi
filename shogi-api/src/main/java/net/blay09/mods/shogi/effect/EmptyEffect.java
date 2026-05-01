package net.blay09.mods.shogi.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;

/**
 * Effect that always resolves to a {@link ShogiEmpty} marker failure.
 */
public class EmptyEffect implements ShogiEffect<Object>, ShogiEmpty {

    /**
     * Shared singleton instance.
     */
    public static final EmptyEffect INSTANCE = new EmptyEffect();
    /**
     * Identifier used for the no-op effect type.
     */
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "noop");
    /**
     * Codec for serializing/deserializing no-op effects.
     */
    public static final MapCodec<EmptyEffect> MAP_CODEC = MapCodec.unit(INSTANCE);

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    /**
     * Returns a right either with this no-op marker.
     *
     * @param context evaluation context
     * @return right either containing this marker instance
     */
    @Override
    public Either<?, ? extends ShogiEmpty> apply(ShogiContext context) {
        return Either.right(this);
    }
}
