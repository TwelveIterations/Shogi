package net.blay09.mods.shogi.common.platform;

import net.minecraft.world.entity.player.Player;

public interface ShogiEventListener {
    void onPlayerDisconnected(Player player);
}
