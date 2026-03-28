package net.blay09.mods.shogi.neoforge;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NeoForgeShogiRuntime implements ShogiRuntime {
    private final Map<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> reloadListeners = new LinkedHashMap<>();

    public NeoForgeShogiRuntime() {
        NeoForge.EVENT_BUS.addListener(this::onAddServerReloadListeners);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void registerServerReloadListener(Identifier listenerId, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        reloadListeners.put(listenerId, factory);
    }

    @Override
    public void sendPacket(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    private void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        reloadListeners.forEach((id, factory) -> event.addListener(id, factory.apply(event.getServerResources().getRegistryLookup())));
    }
}
