package net.blay09.mods.shogi.common.effect.variable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MissingVariableFailure(String name) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MissingVariableFailure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MissingVariableFailure::name,
            MissingVariableFailure::new
    );
}
