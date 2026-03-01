package net.blay09.mods.shogi.fabric.client;

import net.blay09.mods.shogi.client.ShogiClient;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.sync.ShogiStreamCodecs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricShogiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final var events = ShogiClient.initialize();
        ClientPlayNetworking.registerGlobalReceiver(ShogiValueResultPayload.TYPE, (payload, context) -> context.client().schedule(() -> {
            if (!ShogiStreamCodecs.containsUnknown(payload.payload())) {
                events.onValueReceived(payload.scope(), payload.identifier(), payload.payload());
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> events.onDisconnected());
    }
}
