package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.command.ShogiCommand;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricShogi implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(ShogiValueResultPayload.TYPE, ShogiValueResultPayload.STREAM_CODEC);

        final var eventListener = ShogiCommon.initialize();

        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> eventListener.onPlayerDisconnected(listener.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ShogiCommand.register(dispatcher));
    }
}
