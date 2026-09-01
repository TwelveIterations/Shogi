package net.blay09.mods.shogi.common.effect.condition.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public record HasMobEffect(HolderSet<MobEffect> effect) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "has_mob_effect");
    public static final MapCodec<HasMobEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.MOB_EFFECT, BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), false).fieldOf("effect").forGetter(HasMobEffect::effect)
    ).apply(instance, HasMobEffect::new));

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var entity = context.requireEntity();
        if (entity instanceof LivingEntity livingEntity) {
            return Either.left(effect.stream().anyMatch(livingEntity::hasEffect));
        }
        return Either.left(false);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
