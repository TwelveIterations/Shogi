package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.effect.failure.ShogiDeferred;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

public record IsStructure(HolderSet<Structure> structure) implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_structure");
    public static final MapCodec<IsStructure> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.STRUCTURE, false).fieldOf("structure").forGetter(IsStructure::structure)
    ).apply(instance, IsStructure::new));

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var pos = context.requireBlockPos();
        if (level instanceof ServerLevel serverLevel) {
            final var structureManager = serverLevel.structureManager();
            final var structureStart = structureManager.getStructureWithPieceAt(pos, structure);
            return Either.left(structureManager.structureHasPieceAt(pos, structureStart));
        }
        return Either.right(ShogiDeferred.INSTANCE);
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
