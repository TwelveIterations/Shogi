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
import net.minecraftforge.network.event.RegisterPayloadHandlersEvent;

@Mod(value = "shogi")
public class ForgeShogi {
    private final ShogiEventListener events;

    public ForgeShogi(FMLJavaModLoadingContext context) {
        events = ShogiCommon.initialize();
        context.getModEventBus().addListener(this::onRegisterPayloadHandlers);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        if (FMLEnvironment.dist.isClient()) {
            ForgeShogiClient.init(context.getModEventBus());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ShogiCommand.register(event.getDispatcher());
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(ShogiValueResultPayload.TYPE, ShogiValueResultPayload.STREAM_CODEC);
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        events.onPlayerDisconnected(event.getEntity());
    }
}
