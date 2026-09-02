package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

public class IsFluidSource implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_fluid_source");
    private static final IsFluidSource INSTANCE = new IsFluidSource();
    public static final MapCodec<IsFluidSource> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        return Either.left(context.requireFluidState().isSource());
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
