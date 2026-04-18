package net.blay09.mods.shogi.fabric.client;

import net.blay09.mods.shogi.client.ShogiClient;
import net.blay09.mods.shogi.common.ShogiClientRuleReloadListener;
import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public class FabricShogiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final var events = ShogiClient.initialize();
        final var runtime = ShogiRuntimeSpi.get();
        ClientPlayNetworking.registerGlobalReceiver(ShogiValueResultPayload.TYPE, (payload, context) -> context.client().schedule(() -> {
            if (!ShogiStreamCodecs.containsUnknown(payload.payload())) {
                events.onValueReceived(payload.scope(), payload.identifier(), payload.payload());
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> events.onDisconnected());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                ShogiCommon.id("client_rule_reloader"),
                new ShogiClientRuleReloadListener(runtime.getConfigDirectory(), () -> {
                    final var connection = Minecraft.getInstance().getConnection();
                    return connection != null ? connection.registryAccess() : null;
                })
        );
    }
}
