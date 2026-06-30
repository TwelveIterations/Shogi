package net.blay09.mods.shogi.common.effect.failure;

import net.blay09.mods.shogi.coercion.ComponentHolder;
import net.blay09.mods.shogi.effect.failure.ShogiFatalFailure;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public record RefusalInformation(Component message) implements ShogiFatalFailure, ComponentHolder {
    public static final StreamCodec<RegistryFriendlyByteBuf, RefusalInformation> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            RefusalInformation::message,
            RefusalInformation::new
    );

    @Override
    public Component component() {
        return message;
    }
}
