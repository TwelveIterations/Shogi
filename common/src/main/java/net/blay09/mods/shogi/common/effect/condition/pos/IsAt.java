package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record IsAt(BlockPos pos) implements ShogiEffect<Boolean> {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_at");
    public static final MapCodec<IsAt> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(IsAt::pos)
    ).apply(instance, IsAt::new));

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var currentPos = context.requireBlockPos();
        return Either.left(pos.equals(currentPos));
    }
}
