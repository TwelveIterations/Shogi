package net.blay09.mods.shogi.forge;

import net.blay09.mods.shogi.common.ShogiCommon;
import net.minecraftforge.fml.common.Mod;
import net.blay09.mods.shogi.common.command.ShogiCommand;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.common.platform.ShogiEventListener;
import net.blay09.mods.shogi.forge.client.ForgeShogiClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

@Mod(value = "shogi")
public class ForgeShogi {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ShogiValueResultPayload.TYPE.id())
            .networkProtocolVersion(1)
            .optional()
            .simpleChannel();

    private final ShogiEventListener events;

    public ForgeShogi(FMLJavaModLoadingContext context) {
        events = ShogiCommon.initialize();
        registerPayloadHandlers();
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        if (FMLEnvironment.dist.isClient()) {
            ForgeShogiClient.init(context.getModEventBus());
        }
        CHANNEL.build();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ShogiCommand.register(event.getDispatcher());
    }

    private void registerPayloadHandlers() {
        CHANNEL.messageBuilder(ShogiValueResultPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .codec(ShogiValueResultPayload.STREAM_CODEC)
                .consumerMainThread((payload, context) -> {
                    if (context.isClientSide()) {
                        ForgeShogiClient.handleValueResult(payload);
                    }
                })
                .add();
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        events.onPlayerDisconnected(event.getEntity());
    }
}
