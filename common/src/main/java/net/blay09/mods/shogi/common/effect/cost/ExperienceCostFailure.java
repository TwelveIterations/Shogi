package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperienceCostFailure(int available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceCostFailure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperienceCostFailure::available,
            ByteBufCodecs.VAR_INT,
            ExperienceCostFailure::required,
            ExperienceCostFailure::new
    );
}
