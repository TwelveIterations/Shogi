package net.blay09.mods.shogi;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.internal.MinimalShogiFactories;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Factory abstraction for constructing Shogi scopes and values.
 */
public interface ShogiFactories {
    /**
     * Creates a new scope for a namespace of effect registrations and value resolution.
     *
     * @param identifier the scope identifier
     * @return the created scope
     */
    ShogiScope scope(ResourceLocation identifier);

    /**
     * Returns a scope for a namespace of effect registrations and value resolution, creating it if needed, and runs
     * the configuration callback for the resulting scope.
     *
     * @param identifier the scope identifier
     * @param configure  configuration callback for the scope
     * @return the scope
     */
    ShogiScope scope(ResourceLocation identifier, Consumer<ShogiScope> configure);

    /**
     * Creates a value that resolves through a scope and falls back to a default provider.
     *
     * @param identifier      the value identifier
     * @param scope           the scope used for resolving overrides
     * @param defaultProvider fallback provider used when no override applies
     * @param <TContext>      the context type supplied during resolution
     * @param <TSuccess>      the expected success type from the default provider
     * @return a Shogi value wrapper
     */
    <TContext, TSuccess> ShogiValue<TContext, ?> value(ResourceLocation identifier, ShogiScope scope, Function<TContext, TSuccess> defaultProvider);

    /**
     * Creates a value whose default provider already returns an Either success/failure payload.
     *
     * @param identifier  the value identifier
     * @param scope       the scope used for resolving overrides
     * @param defaultRule fallback resolver returning an either payload
     * @param <TContext>  the context type supplied during resolution
     * @param <TSuccess>  the success type
     * @return a Shogi value wrapper
     */
    <TContext, TSuccess> ShogiValue<TContext, ?> maybe(ResourceLocation identifier, ShogiScope scope, Function<TContext, Either<TSuccess, ?>> defaultRule);

    /**
     * Creates a factory implementation.
     * <p>
     * This first attempts to load the runtime implementation from {@code shogi-common}, and falls back to
     * a minimal API-only implementation when the runtime classes are unavailable.
     *
     * @return a factories implementation suitable for the current environment
     * @throws RuntimeException if the runtime implementation is present but cannot be instantiated
     */
    static ShogiFactories create() {
        try {
            return (ShogiFactories) Class.forName("net.blay09.mods.shogi.common.ShogiFactoriesImpl").getConstructor().newInstance();
        } catch (ClassNotFoundException ignored) {
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return new MinimalShogiFactories();
    }
}
