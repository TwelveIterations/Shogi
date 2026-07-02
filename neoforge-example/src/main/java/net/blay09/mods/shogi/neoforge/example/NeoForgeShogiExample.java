package net.blay09.mods.shogi.neoforge.example;

import net.blay09.mods.shogi.Shogi;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static net.blay09.mods.shogi.common.ShogiCommon.id;

@Mod(value = "shogi_example", dist = Dist.CLIENT)
public class NeoForgeShogiExample {

    public NeoForgeShogiExample(ModContainer modContainer, IEventBus modEventBus) {
        final var intValue = Shogi.intValue(id("int"), ignored -> 123);
        final var floatValue = Shogi.floatValue(id("float"), ignored -> 13.37f);
        final var stringValue = Shogi.stringValue(id("string"), ignored -> "Hello World");
        final var booleanValue = Shogi.booleanValue(id("boolean"), ignored -> false).networked();
        final var nameValue = Shogi.componentValue(id("name"), Player::getName);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            for (final var player : event.getServer().getPlayerList().getPlayers()) {
                if (player.isShiftKeyDown()) {
                    System.out.println(intValue.get(player));
                    System.out.println(floatValue.get(player));
                    System.out.println(stringValue.get(player));
                    System.out.println(booleanValue.get(player));
                    System.out.println(nameValue.get(player));
                }
            }
        });
    }
}
