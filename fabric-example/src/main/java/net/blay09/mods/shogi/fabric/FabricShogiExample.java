package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.Shogi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class FabricShogiExample implements ModInitializer {

    @Override
    public void onInitialize() {
        final var intValue = Shogi.intValue(id("int"), _ -> 123);
        final var floatValue = Shogi.floatValue(id("float"), _ -> 13.37f);
        final var stringValue = Shogi.stringValue(id("string"), _ -> "Hello World");
        final var booleanValue = Shogi.booleanValue(id("boolean"), _ -> false).networked();
        final var nameValue = Shogi.componentValue(id("name"), Player::getName);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (final var player : server.getPlayerList().getPlayers()) {
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

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("shogi", path);
    }
}
