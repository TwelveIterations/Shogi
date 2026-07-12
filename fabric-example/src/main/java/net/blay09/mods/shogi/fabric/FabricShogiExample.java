package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class FabricShogiExample implements ModInitializer {

    @Override
    public void onInitialize() {
        final var exampleScope = Shogi.scope(id("example"));
        final var intValue = exampleScope.intValue(id("int"), _ -> 123);
        final var floatValue = exampleScope.floatValue(id("float"), _ -> 13.37f);
        final var stringValue = exampleScope.stringValue(id("string"), _ -> "Hello World");
        final var booleanValue = exampleScope.booleanValue(id("boolean"), _ -> false).networked();
        final var nameValue = exampleScope.componentValue(id("name"), Player::getName);

        final var clientScope = Shogi.scope(id("client"), ShogiScope::loadOnClient);
        final var clientIntValue = clientScope.intValue(id("int"), _ -> 1337);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (final var player : server.getPlayerList().getPlayers()) {
                if (player.isShiftKeyDown()) {
                    System.out.println(intValue.get(player));
                    System.out.println(floatValue.get(player));
                    System.out.println(stringValue.get(player));
                    System.out.println(booleanValue.get(player));
                    System.out.println(nameValue.get(player));
                    System.out.println(clientIntValue.get(player));
                }
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("shogi", path);
    }
}
