package net.blay09.mods.shogi.common.effect.failure;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public record FailureInformation(Component message) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FailureInformation> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            FailureInformation::message,
            FailureInformation::new
    );
}
