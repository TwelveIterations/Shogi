package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record IsNearBlockEntity(HolderSet<BlockEntityType<?>> blockEntityType, float distance) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_near_block_entity");
    public static final MapCodec<IsNearBlockEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec(), false).fieldOf("block_entity_type").forGetter(IsNearBlockEntity::blockEntityType),
            Codec.FLOAT.fieldOf("distance").forGetter(IsNearBlockEntity::distance)
    ).apply(instance, IsNearBlockEntity::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        final int chunkRadius = (int) Math.ceil(distance / 16f);
        final double maxDistance = distance * distance;
        final int centerChunkX = SectionPos.blockToSectionCoord(pos.getX());
        final int centerChunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                for (final var blockEntityPos : level.getChunk(chunkX, chunkZ).getBlockEntitiesPos()) {
                    if (blockEntityPos.distSqr(pos) > maxDistance) {
                        continue;
                    }

                    final var nearbyBlockEntity = level.getBlockEntity(blockEntityPos);
                    if (nearbyBlockEntity != null && nearbyBlockEntity.is(blockEntityType)) {
                        return Either.left(true);
                    }
                }
            }
        }

        return Either.left(false);
    }
}
