package net.blay09.mods.shogi.fabric;

import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class FabricShogiExample implements ModInitializer {

    @Override
    public void onInitialize() {
        final var intValue = Shogi.intValue(id("int"), ignored -> 123);
        final var floatValue = Shogi.floatValue(id("float"), ignored -> 13.37f);
        final var stringValue = Shogi.stringValue(id("string"), ignored -> "Hello World");
        final var booleanValue = Shogi.booleanValue(id("boolean"), ignored -> false).networked();
        final var nameValue = Shogi.componentValue(id("name"), Player::getName);

        final var clientScope = Shogi.scope(id("client"), ShogiScope::loadOnClient);
        final var clientIntValue = clientScope.intValue(id("int"), ignored -> 1337);

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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shogi", path);
    }
}
