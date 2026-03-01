package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.function.Function;

public class FabricShogiRuntime implements ShogiRuntime {
    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerServerReloadListener(Identifier listenerId, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        DataResourceLoader.get().registerReloadListener(listenerId, factory);
    }

    @Override
    public void sendPacket(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return false;
    }
}
