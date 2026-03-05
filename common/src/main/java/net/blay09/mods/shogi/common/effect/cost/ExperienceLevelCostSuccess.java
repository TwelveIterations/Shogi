package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperienceLevelCostSuccess(int amount) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceLevelCostSuccess> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExperienceLevelCostSuccess::amount,
            ExperienceLevelCostSuccess::new
    );
}
