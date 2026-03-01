package net.blay09.mods.shogi.client;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.client.platform.ShogiClientEventListener;
import net.blay09.mods.shogi.internal.ShogiScopeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShogiClient implements ShogiClientEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ShogiClient.class);

    public static ShogiClient initialize() {
        return new ShogiClient();
    }

    @Override
    public void onValueReceived(Identifier scopeId, Identifier identifier, Either<?, ?> payload) {
        ShogiScopeRegistry.get(scopeId)
                .ifPresentOrElse(scope -> scope.getNetworkCache().valueReceived(identifier, payload),
                        () -> logger.warn("Ignoring synced Shogi value '{}' for unknown scope '{}'", identifier, scopeId));
    }

    @Override
    public void onDisconnected() {
        for (final var scope : ShogiScopeRegistry.getAll()) {
            scope.getNetworkCache().invalidateAll();
        }
    }
}
