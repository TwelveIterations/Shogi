package net.blay09.mods.shogi.context.executor.aggregate;

import net.minecraft.resources.Identifier;

/**
 * Typed key used for storing aggregate values in a {@link net.blay09.mods.shogi.context.ShogiContext}.
 *
 * @param identifier unique aggregate key identifier
 * @param <T> aggregate value type
 */
@SuppressWarnings("unused")
public record AggregateKey<T>(Identifier identifier) {
    /**
     * Creates an aggregate key for the given identifier.
     *
     * @param identifier unique key identifier
     * @param <T> aggregate value type
     * @return a new aggregate key
     */
    public static <T> AggregateKey<T> of(Identifier identifier) {
        return new AggregateKey<>(identifier);
    }
}
