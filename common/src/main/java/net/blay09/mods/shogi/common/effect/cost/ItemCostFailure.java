package net.blay09.mods.shogi.common.effect.cost;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;

public record ItemCostFailure(
        HolderSet<Item> item,
        int available,
        int required
) {
}
