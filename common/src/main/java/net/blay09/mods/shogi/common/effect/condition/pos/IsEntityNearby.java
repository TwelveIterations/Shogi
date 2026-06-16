package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record IsEntityNearby(HolderSet<EntityType<?>> entity, float distance,
                             int min) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_entity_nearby");
    public static final MapCodec<IsEntityNearby> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false).fieldOf("entity").forGetter(IsEntityNearby::entity),
            Codec.FLOAT.fieldOf("distance").forGetter(IsEntityNearby::distance),
            Codec.INT.fieldOf("min").orElse(1).forGetter(IsEntityNearby::min)
    ).apply(instance, IsEntityNearby::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var selfEntity = context.entity();
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        final var entities = level.getEntities(selfEntity, AABB.ofSize(Vec3.atCenterOf(pos), distance, distance, distance),it -> entity.contains(it.typeHolder()));
        return Either.left(entities.size() >= min);
    }
}
