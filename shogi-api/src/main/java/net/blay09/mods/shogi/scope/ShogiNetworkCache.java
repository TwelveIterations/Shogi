package net.blay09.mods.shogi.scope;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Cache abstraction used for network-synchronized Shogi values.
 */
public interface ShogiNetworkCache {
    /**
     * Marks a value identifier as network-synchronized.
     *
     * @param identifier value identifier
     */
    void addNetworkedValue(Identifier identifier);

    /**
     * Called after a value is resolved to allow caching or substitution.
     *
     * @param identifier value identifier
     * @param context resolution context
     * @param value resolved either payload
     * @param <TSuccess> success type
     * @param <TFailure> failure type
     * @return payload to continue using for this resolution
     */
    <TSuccess, TFailure> Either<TSuccess, TFailure> valueResolved(Identifier identifier, ShogiContext context, Either<TSuccess, TFailure> value);

    /**
     * Stores a payload received from a remote source.
     *
     * @param identifier value identifier
     * @param payload received payload
     */
    void valueReceived(Identifier identifier, Either<?, ?> payload);

    /**
     * Looks up the latest remote payload for a value and context.
     *
     * @param identifier value identifier
     * @param context lookup context
     * @return the remote payload if available
     */
    Optional<Either<?, ?>> getRemoteValue(Identifier identifier, ShogiContext context);

    /**
     * Invalidates all cached payloads.
     */
    void invalidateAll();

    /**
     * Invalidates cached payloads related to a player.
     *
     * @param player player whose cache entries should be invalidated
     */
    void invalidate(Player player);

    /**
     * No-op cache implementation.
     */
    ShogiNetworkCache NONE = new ShogiNetworkCache() {
        @Override
        public <TSuccess, TFailure> Either<TSuccess, TFailure> valueResolved(Identifier identifier, ShogiContext context, Either<TSuccess, TFailure> value) {
            return value;
        }

        @Override
        public void valueReceived(Identifier identifier, Either<?, ?> payload) {
        }

        @Override
        public Optional<Either<?, ?>> getRemoteValue(Identifier identifier, ShogiContext context) {
            return Optional.empty();
        }

        @Override
        public void invalidateAll() {
        }

        @Override
        public void invalidate(Player player) {
        }

        @Override
        public void addNetworkedValue(Identifier identifier) {
            throw new UnsupportedOperationException("Unable to add networked value on NO-OP network cache.");
        }
    };
}
