package net.blay09.mods.shogi.client.platform;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;

public interface ShogiClientEventListener {

    void onValueReceived(Identifier scopeId, Identifier identifier, Either<?, ?> payload);

    void onDisconnected();

    ShogiClientEventListener NONE = new ShogiClientEventListener() {
        @Override
        public void onValueReceived(Identifier scopeId, Identifier identifier, Either<?, ?> payload) {
        }

        @Override
        public void onDisconnected() {
        }
    };
}
