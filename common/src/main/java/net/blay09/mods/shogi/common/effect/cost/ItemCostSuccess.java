package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record ItemCostSuccess(HolderSet<Item> item, int count) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCostSuccess> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ITEM),
            ItemCostSuccess::item,
            ByteBufCodecs.VAR_INT,
            ItemCostSuccess::count,
            ItemCostSuccess::new
    );
}
