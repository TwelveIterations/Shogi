package net.blay09.mods.shogi.common.effect.condition.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record HasEnchantment(ResourceKey<Enchantment> enchantment,
                             int level) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "has_enchantment");
    public static final MapCodec<HasEnchantment> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(HasEnchantment::enchantment),
            Codec.INT.fieldOf("level").orElse(1).forGetter(HasEnchantment::level)
    ).apply(instance, HasEnchantment::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var itemStack = context.itemStack();
        return Either.left(level.registryAccess().lookup(Registries.ENCHANTMENT)
                .flatMap(it -> it.get(enchantment))
                .map(it -> EnchantmentHelper.getItemEnchantmentLevel(it, itemStack))
                .map(it -> it >= this.level)
                .orElse(false));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
