package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record ItemCostInformation(
        HolderSet<Item> item,
        int available,
        int required
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCostInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ITEM),
            ItemCostInformation::item,
            ByteBufCodecs.VAR_INT,
            ItemCostInformation::available,
            ByteBufCodecs.VAR_INT,
            ItemCostInformation::required,
            ItemCostInformation::new
    );
}
