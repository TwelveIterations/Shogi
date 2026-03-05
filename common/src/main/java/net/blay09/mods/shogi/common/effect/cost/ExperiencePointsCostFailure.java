package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperiencePointsCostFailure(int available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperiencePointsCostFailure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperiencePointsCostFailure::available,
            ByteBufCodecs.VAR_INT,
            ExperiencePointsCostFailure::required,
            ExperiencePointsCostFailure::new
    );
}
