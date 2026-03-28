package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

public class GetLightLevel implements ShogiEffect<Integer> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "get_light_level");
    private static final GetLightLevel INSTANCE = new GetLightLevel();
    public static final MapCodec<GetLightLevel> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Integer, Throwable> apply(ShogiContext context) {
        final var level = context.requireLevel();
        final var blockPos = context.requireBlockPos();
        return Either.left(level.getMaxLocalRawBrightness(blockPos));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }

}
