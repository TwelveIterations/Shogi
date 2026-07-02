package net.blay09.mods.shogi.client.platform;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;

public interface ShogiClientEventListener {

    void onValueReceived(ResourceLocation scopeId, ResourceLocation identifier, Either<?, ?> payload);

    void onDisconnected();

    ShogiClientEventListener NONE = new ShogiClientEventListener() {
        @Override
        public void onValueReceived(ResourceLocation scopeId, ResourceLocation identifier, Either<?, ?> payload) {
        }

        @Override
        public void onDisconnected() {
        }
    };
}
