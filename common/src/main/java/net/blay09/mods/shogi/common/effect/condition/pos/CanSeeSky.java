package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.resources.ResourceLocation;

public class CanSeeSky implements ShogiEffect<Boolean> {

    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("shogi", "can_see_sky");
    private static final CanSeeSky INSTANCE = new CanSeeSky();
    public static final MapCodec<CanSeeSky> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var blockPos = context.requireBlockPos();
        return Either.left(level.canSeeSky(blockPos));
    }

    @Override
    public ResourceLocation identifier() {
        return IDENTIFIER;
    }

}
