package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperiencePointsCostSuccess(int amount) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperiencePointsCostSuccess> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperiencePointsCostSuccess::amount,
            ExperiencePointsCostSuccess::new
    );
}
