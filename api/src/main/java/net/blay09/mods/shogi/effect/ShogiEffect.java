package net.blay09.mods.shogi.effect;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Executable unit used by Shogi rules.
 *
 * @param <TResult> success type returned by this effect
 */
public interface ShogiEffect<TResult> extends Function<ShogiContext, Either<? extends TResult, ?>>, Predicate<ShogiContext> {
    /**
     * Returns the unique identifier for this effect type.
     *
     * @return effect identifier
     */
    Identifier identifier();

    /**
     * Evaluates this effect as a boolean predicate.
     * <p>
     * Resolution failures and thrown exceptions are treated as {@code false}.
     *
     * @param context evaluation context
     * @return {@code true} when the effect resolves to a truthy success value
     */
    @Override
    default boolean test(ShogiContext context) {
        try {
            return apply(context).mapLeft(Coercion.BOOLEAN).left().orElse(false);
        } catch (Throwable throwable) {
            return false;
        }
    }

    static <T> ShogiEffect<T> simple(Identifier identifier, Function<ShogiContext, T> supplier) {
        return new ShogiEffect<>() {
            @Override
            public Identifier identifier() {
                return identifier;
            }

            @Override
            public Either<? extends T, ?> apply(ShogiContext context) {
                return Either.left(supplier.apply(context));
            }
        };
    }

    static <T> ShogiEffect<T> simple(Identifier identifier, Supplier<T> supplier) {
        return new ShogiEffect<>() {
            @Override
            public Identifier identifier() {
                return identifier;
            }

            @Override
            public Either<? extends T, ?> apply(ShogiContext context) {
                return Either.left(supplier.get());
            }
        };
    }
}
