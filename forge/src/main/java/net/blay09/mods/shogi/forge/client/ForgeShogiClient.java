package net.blay09.mods.shogi.forge.client;

import net.blay09.mods.shogi.client.ShogiClient;
import net.blay09.mods.shogi.client.platform.ShogiClientEventListener;
import net.blay09.mods.shogi.common.ShogiClientRuleReloadListener;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeShogiClient {
    private static ShogiClientEventListener events;

    public static void init(IEventBus modEventBus) {
        events = ShogiClient.initialize();
        modEventBus.addListener(ForgeShogiClient::onRegisterClientReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(ForgeShogiClient::onClientLoggedOut);
    }

    public static void handleValueResult(ShogiValueResultPayload payload) {
        if (!ShogiStreamCodecs.containsUnknown(payload.payload())) {
            events.onValueReceived(payload.scope(), payload.identifier(), payload.payload());
        }
    }

    private static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        events.onDisconnected();
    }

    private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        final var runtime = ShogiRuntimeSpi.get();
        event.registerReloadListener(new ShogiClientRuleReloadListener(runtime.getConfigDirectory(), () -> {
            final var connection = Minecraft.getInstance().getConnection();
            return connection != null ? connection.registryAccess() : null;
        }));
    }
}
