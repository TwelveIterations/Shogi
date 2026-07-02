package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

public record IsMobNearby(float distance, int min) implements ShogiEffect<Boolean> {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_mob_nearby");
    public static final MapCodec<IsMobNearby> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("distance").forGetter(IsMobNearby::distance),
            Codec.INT.fieldOf("min").orElse(1).forGetter(IsMobNearby::min)
    ).apply(instance, IsMobNearby::new));

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        final var entities = level.getEntitiesOfClass(Mob.class, AABB.ofSize(pos.getCenter(), distance, distance, distance));
        return Either.left(entities.size() >= min);
    }
}
