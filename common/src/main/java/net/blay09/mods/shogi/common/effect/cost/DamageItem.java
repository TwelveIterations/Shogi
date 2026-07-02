package net.blay09.mods.shogi.common.effect.cost;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.EffectArgumentCodecs;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record DamageItem(ShogiEffect<?> amount) implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "damage_item");

    public static MapCodec<DamageItem> mapCodec(ShogiScope scope) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                EffectArgumentCodecs.effectOrConstant(scope).fieldOf("amount").forGetter(DamageItem::amount)
        ).apply(instance, DamageItem::new));
    }

    @Override
    public Either<Boolean, Object> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return Either.left(false);
        }

        final var itemStack = context.itemStack();
        final int damageAmount = amount.apply(context).mapLeft(Coercion.NON_NEGATIVE_INT).orThrow();
        if (damageAmount <= 0 || itemStack.isEmpty() || !itemStack.isDamageableItem()) {
            return Either.left(false);
        }

        context.execute(IDENTIFIER, () -> {
            var equipmentSlot = findEquipmentSlotForItem(livingEntity, itemStack);
            if (equipmentSlot != null) {
                itemStack.hurtAndBreak(damageAmount, livingEntity, equipmentSlot);
            } else if (entity instanceof ServerPlayer serverPlayer) {
                itemStack.hurtAndBreak(damageAmount, serverPlayer.serverLevel(), serverPlayer, ignored -> {
                });
            }
        });
        return Either.left(true);
    }

    @Nullable
    private EquipmentSlot findEquipmentSlotForItem(LivingEntity entity, ItemStack itemStack) {
        for (final var slot : EquipmentSlot.values()) {
            final var slotItem = entity.getItemBySlot(slot);
            if (slotItem == itemStack) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
