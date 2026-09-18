package net.blay09.mods.shogi.common.platform;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface ShogiEventListener {
    void onPlayerDisconnected(Player player);

    void onLivingEntityDeath(LivingEntity entity, DamageSource damageSource);
}
