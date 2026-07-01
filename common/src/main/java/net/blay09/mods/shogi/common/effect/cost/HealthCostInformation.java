package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HealthCostInformation(float available, float required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, HealthCostInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            HealthCostInformation::available,
            ByteBufCodecs.FLOAT,
            HealthCostInformation::required,
            HealthCostInformation::new
    );
}
