package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperiencePointsCostInformation(long available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperiencePointsCostInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ExperiencePointsCostInformation::available,
            ByteBufCodecs.VAR_INT,
            ExperiencePointsCostInformation::required,
            ExperiencePointsCostInformation::new
    );
}
