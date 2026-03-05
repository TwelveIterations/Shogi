package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperienceLevelCostInformation(int available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceLevelCostInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperienceLevelCostInformation::available,
            ByteBufCodecs.VAR_INT,
            ExperienceLevelCostInformation::required,
            ExperienceLevelCostInformation::new
    );
}
