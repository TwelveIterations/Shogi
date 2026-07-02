package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record IsBlockEntity(HolderSet<BlockEntityType<?>> blockEntityType) implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_block_entity");
    public static final MapCodec<IsBlockEntity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HolderSetCodec.create(Registries.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec(), false).fieldOf("block_entity_type").forGetter(IsBlockEntity::blockEntityType)
    ).apply(instance, IsBlockEntity::new));

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var blockEntity = context.requireBlockEntity();
        return Either.left(blockEntityType.contains(blockEntity.getType().builtInRegistryHolder()));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }
}
