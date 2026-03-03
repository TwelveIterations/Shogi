package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperienceLevelCostFailure(int available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceLevelCostFailure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperienceLevelCostFailure::available,
            ByteBufCodecs.VAR_INT,
            ExperienceLevelCostFailure::required,
            ExperienceLevelCostFailure::new
    );
}
