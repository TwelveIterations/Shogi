package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.common.platform.ShogiRuntime;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class FabricShogiRuntime implements ShogiRuntime {
    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerServerReloadListener(ResourceLocation listenerId, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(listenerId, registries -> identifiable(listenerId, factory.apply(registries)));
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

    public static IdentifiableResourceReloadListener identifiable(ResourceLocation listenerId, PreparableReloadListener listener) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return listenerId;
            }

            @Override
            public Collection<ResourceLocation> getFabricDependencies() {
                return IdentifiableResourceReloadListener.super.getFabricDependencies();
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return listener.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public String getName() {
                return listener.getName();
            }
        };
    }
}
