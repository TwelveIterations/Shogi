package net.blay09.mods.shogi.neoforge;

import net.blay09.mods.shogi.common.ShogiCommon;
import net.blay09.mods.shogi.common.command.ShogiCommand;
import net.blay09.mods.shogi.common.network.ShogiValueResultPayload;
import net.blay09.mods.shogi.common.platform.ShogiEventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(value = "shogi")
public class NeoForgeShogi {
    private final ShogiEventListener events;

    public NeoForgeShogi(ModContainer modContainer, IEventBus modEventBus) {
        events = ShogiCommon.initialize();
        modEventBus.addListener(this::onRegisterPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
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
