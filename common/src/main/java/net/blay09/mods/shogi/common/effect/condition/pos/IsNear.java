package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record IsNear(BlockPos pos, int distance) implements ShogiEffect<Boolean> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_near");
    public static final MapCodec<IsNear> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(IsNear::pos),
            Codec.INT.fieldOf("distance").forGetter(IsNear::distance)
    ).apply(instance, IsNear::new));

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var currentPos = context.requireBlockPos();
        final var currentDistance = pos.distSqr(currentPos);
        return Either.left(currentDistance <= distance * distance);
    }
}
