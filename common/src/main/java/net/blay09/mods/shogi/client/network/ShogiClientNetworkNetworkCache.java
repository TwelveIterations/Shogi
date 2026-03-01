package net.blay09.mods.shogi.client.network;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiNetworkCache;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ShogiClientNetworkNetworkCache implements ShogiNetworkCache {

    private final Map<Identifier, Either<?, ?>> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<Either<?, ?>> getRemoteValue(Identifier identifier, ShogiContext context) {
        return Optional.ofNullable(cache.get(identifier));
    }

    @Override
    public void addNetworkedValue(Identifier identifier) {
    }

    @Override
    public <TSuccess, TFailure> Either<TSuccess, TFailure> valueResolved(Identifier identifier, ShogiContext context, Either<TSuccess, TFailure> value) {
        // We do not store resolved values because this cache is populated by the server.
        return value;
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public void invalidate(Player player) {
        cache.clear();
    }

    public void valueReceived(Identifier identifier, Either<?, ?> payload) {
        cache.put(identifier, payload);
    }
}
