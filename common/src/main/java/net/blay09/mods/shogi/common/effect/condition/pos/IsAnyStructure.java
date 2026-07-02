package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class IsAnyStructure implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_any_structure");
    private static final IsAnyStructure INSTANCE = new IsAnyStructure();
    public static final MapCodec<IsAnyStructure> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        if (level instanceof ServerLevel serverLevel) {
            final var structureManager = serverLevel.structureManager();
            final var structures = structureManager.getAllStructuresAt(pos);
            for (final var structure : structures.keySet()) {
                final var structureStart = structureManager.getStructureAt(pos, structure);
                if (structureManager.structureHasPieceAt(pos, structureStart)) {
                    return Either.left(true);
                }
            }

            return Either.left(false);
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

}
