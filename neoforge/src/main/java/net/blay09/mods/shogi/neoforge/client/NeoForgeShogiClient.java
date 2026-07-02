package net.blay09.mods.shogi.neoforge.client;

import net.blay09.mods.shogi.client.ShogiClient;
import net.blay09.mods.shogi.client.platform.ShogiClientEventListener;
import net.blay09.mods.shogi.common.ShogiClientRuleReloadListener;
import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.platform.ShogiRuntimeSpi;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "shogi", dist = Dist.CLIENT)
public class NeoForgeShogiClient {
    private final ShogiClientEventListener events;

    public NeoForgeShogiClient(ModContainer modContainer, IEventBus modEventBus) {
        events = ShogiClient.initialize();
        modEventBus.addListener(this::onAddClientReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onClientLoggedOut);
    }

    private void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        events.onDisconnected();
    }

    private void onAddClientReloadListeners(RegisterClientReloadListenersEvent event) {
        final var runtime = ShogiRuntimeSpi.get();
        event.registerReloadListener(new ShogiClientRuleReloadListener(runtime.getConfigDirectory(), () -> {
            final var connection = Minecraft.getInstance().getConnection();
            return connection != null ? connection.registryAccess() : null;
        }));
    }
}
