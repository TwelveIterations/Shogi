package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record ItemCostFailure(
        HolderSet<Item> item,
        int available,
        int required
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCostFailure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ITEM),
            ItemCostFailure::item,
            ByteBufCodecs.VAR_INT,
            ItemCostFailure::available,
            ByteBufCodecs.VAR_INT,
            ItemCostFailure::required,
            ItemCostFailure::new
    );
}
