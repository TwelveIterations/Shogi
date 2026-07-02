package net.blay09.mods.shogi.common.network;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiNetworkCache;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShogiServerNetworkNetworkCache implements ShogiNetworkCache {
    private static final Logger logger = LoggerFactory.getLogger(ShogiServerNetworkNetworkCache.class);

    private record CacheKey(ResourceLocation identifier, UUID playerId) {
        public static CacheKey of(ResourceLocation identifier, ServerPlayer player) {
            return new CacheKey(identifier, player.getUUID());
        }
    }

    private final Set<ResourceLocation> networkedValues = Sets.newConcurrentHashSet();
    private final Map<CacheKey, Object> cache = new ConcurrentHashMap<>();
    private final ResourceLocation scopeId;

    public ShogiServerNetworkNetworkCache(ResourceLocation scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    public Optional<Either<?, ?>> getRemoteValue(ResourceLocation identifier, ShogiContext context) {
        // We always return empty because we don't want cached values to be used on the server.
        return Optional.empty();
    }

    @Override
    public void addNetworkedValue(ResourceLocation identifier) {
        networkedValues.add(identifier);
    }

    @Override
    public <TSuccess, TFailure> Either<TSuccess, TFailure> valueResolved(ResourceLocation identifier, ShogiContext context, Either<TSuccess, TFailure> value) {
        if (!networkedValues.contains(identifier)) {
            return value;
        }

        if (!(context.entity() instanceof ServerPlayer player)) {
            return value;
        }

        final var key = CacheKey.of(identifier, player);
        final var oldValue = cache.put(key, value);
        if (!ShogiStreamCodecs.canEncodeEither(value)) {
            logger.warn("Skipping sync payload for '{}' because its value type has no registered stream codec", identifier);
            return value;
        }

        if (oldValue == null || !Objects.equals(Either.unwrap((Either<?, ?>) oldValue), Either.unwrap(value))) {
            ShogiRuntimeSpi.get().sendPacket(player, new ShogiValueResultPayload(scopeId, identifier, value));
        }
        return value;
    }

    @Override
    public void valueReceived(ResourceLocation identifier, Either<?, ?> payload) {
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public void invalidate(Player player) {
        cache.keySet().removeIf(key -> key.playerId().equals(player.getUUID()));
    }

}
