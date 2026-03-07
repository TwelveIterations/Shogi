package net.blay09.mods.shogi.common.effect.condition.player;

import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;

public class SimplePlayerEffects {
    public static boolean hasEmptyInventory(ShogiContext context) {
        return context.requirePlayer().getInventory().isEmpty();
    }

    public static boolean isWearingAnyArmor(ShogiContext context) {
        if (context.entity() instanceof LivingEntity livingEntity) {
            for (final var equipmentSlot : EquipmentSlotGroup.ARMOR) {
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    final var stack = livingEntity.getItemBySlot(equipmentSlot);
                    if (!stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
