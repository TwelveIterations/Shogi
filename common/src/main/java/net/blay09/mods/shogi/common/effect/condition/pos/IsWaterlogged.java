package net.blay09.mods.shogi.common.effect.condition.pos;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class IsWaterlogged implements ShogiEffect<Boolean> {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "is_waterlogged");
    private static final IsWaterlogged INSTANCE = new IsWaterlogged();
    public static final MapCodec<IsWaterlogged> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Either<Boolean, Throwable> apply(ShogiContext context) {
        final var blockState = context.requireBlockState();
        return Either.left(blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && blockState.getValue(BlockStateProperties.WATERLOGGED));
    }

    @Override
    public Identifier identifier() {
        return IDENTIFIER;
    }
}
