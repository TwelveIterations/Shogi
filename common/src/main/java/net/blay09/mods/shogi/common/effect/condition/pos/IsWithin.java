package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record IsWithin(BoundingBox bounds) implements ShogiEffect<Boolean> {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "is_within");
    public static final MapCodec<IsWithin> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BoundingBox.CODEC.fieldOf("bounds").forGetter(IsWithin::bounds)
    ).apply(instance, IsWithin::new));

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

    @Override
    public Either<? extends Boolean, ?> apply(ShogiContext context) {
        final var pos = context.requireBlockPos();
        return Either.left(bounds.isInside(pos));
    }
}
