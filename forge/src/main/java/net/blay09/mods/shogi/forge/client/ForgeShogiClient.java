package net.blay09.mods.shogi.forge.client;

import net.blay09.mods.shogi.client.ShogiClient;
import net.blay09.mods.shogi.client.platform.ShogiClientEventListener;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.sync.ShogiStreamCodecs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeShogiClient {
    private static ShogiClientEventListener events;

    public static void init(IEventBus modEventBus) {
        events = ShogiClient.initialize();
        modEventBus.addListener(ForgeShogiClient::onRegisterClientPayloadHandlers);
        MinecraftForge.EVENT_BUS.addListener(ForgeShogiClient::onClientLoggedOut);
    }

    private static void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ShogiValueResultPayload.TYPE, (payload, context) -> {
            if (!ShogiStreamCodecs.containsUnknown(payload.payload())) {
                events.onValueReceived(payload.scope(), payload.identifier(), payload.payload());
            }
        });
    }

    private static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        events.onDisconnected();
    }
}
