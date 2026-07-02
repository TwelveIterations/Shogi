package net.blay09.mods.shogi.common.network;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.client.network.ShogiClientNetworkNetworkCache;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.scope.ShogiNetworkCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ShogiNetworkCacheImpl implements ShogiNetworkCache {

    private final ShogiServerNetworkNetworkCache serverCache;
    private final ShogiClientNetworkNetworkCache clientCache = new ShogiClientNetworkNetworkCache();

    public ShogiNetworkCacheImpl(ResourceLocation scopeId) {
        serverCache = new ShogiServerNetworkNetworkCache(scopeId);
    }

    @Override
    public <TSuccess, TFailure> Either<TSuccess, TFailure> valueResolved(ResourceLocation identifier, ShogiContext context, Either<TSuccess, TFailure> value) {
        return resolveCache(context.level()).valueResolved(identifier, context, value);
    }

    @Override
    public void valueReceived(ResourceLocation identifier, Either<?, ?> payload) {
        clientCache.valueReceived(identifier, payload);
    }

    @Override
    public Optional<Either<?, ?>> getRemoteValue(ResourceLocation identifier, ShogiContext context) {
        return resolveCache(context.level()).getRemoteValue(identifier, context);
    }

    @Override
    public void invalidateAll() {
        clientCache.invalidateAll();
        serverCache.invalidateAll();
    }

    @Override
    public void invalidate(Player player) {
        resolveCache(player.level()).invalidate(player);
    }

    private ShogiNetworkCache resolveCache(@Nullable Level level) {
        if (level == null) {
            return NONE;
        }
        return level.isClientSide() ? clientCache : serverCache;
    }

    @Override
    public void addNetworkedValue(ResourceLocation identifier) {
        serverCache.addNetworkedValue(identifier);
    }
}
