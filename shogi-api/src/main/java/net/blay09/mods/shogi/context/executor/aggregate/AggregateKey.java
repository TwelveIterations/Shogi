package net.blay09.mods.shogi.context.executor.aggregate;

import net.minecraft.resources.ResourceLocation;

/**
 * Typed key used for storing aggregate values in a {@link net.blay09.mods.shogi.context.ShogiContext}.
 *
 * @param identifier unique aggregate key identifier
 * @param <T> aggregate value type
 */
@SuppressWarnings("unused")
public record AggregateKey<T>(ResourceLocation identifier) {
    /**
     * Creates an aggregate key for the given identifier.
     *
     * @param identifier unique key identifier
     * @param <T> aggregate value type
     * @return a new aggregate key
     */
    public static <T> AggregateKey<T> of(ResourceLocation identifier) {
        return new AggregateKey<>(identifier);
    }
}
