package net.blay09.mods.shogi.common.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.function.Function;

public interface ShogiRuntime {
    Path getConfigDirectory();

    void registerServerReloadListener(Identifier listenerId, Function<HolderLookup.Provider, PreparableReloadListener> factory);

    void sendPacket(ServerPlayer player, CustomPacketPayload payload);

    boolean isFakePlayer(Player player);
}
