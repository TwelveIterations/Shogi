package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HungerCostInformation(int available, int required) {
    public static final StreamCodec<RegistryFriendlyByteBuf, HungerCostInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            HungerCostInformation::available,
            ByteBufCodecs.VAR_INT,
            HungerCostInformation::required,
            HungerCostInformation::new
    );
}
