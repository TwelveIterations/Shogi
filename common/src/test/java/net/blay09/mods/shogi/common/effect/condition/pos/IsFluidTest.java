package net.blay09.mods.shogi.common.effect.condition.pos;

import net.blay09.mods.shogi.context.MutableShogiContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsFluidTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final IsFluid IS_WATER = new IsFluid(HolderSet.direct(BuiltInRegistries.FLUID.wrapAsHolder(Fluids.WATER)));

    @Test
    void acceptsFluidStateContext() {
        final var fluidState = Fluids.WATER.defaultFluidState();
        final var context = MutableShogiContext.of(fluidState);

        assertSame(fluidState, context.fluidState());
        assertTrue(IS_WATER.apply(context).left().orElseThrow());
    }

    @Test
    void matchesWaterloggedBlock() {
        final var blockState = Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);

        assertTrue(IS_WATER.apply(MutableShogiContext.of(blockState)).left().orElseThrow());
    }

    @Test
    void doesNotMatchDryBlock() {
        assertFalse(IS_WATER.apply(MutableShogiContext.of(Blocks.STONE.defaultBlockState())).left().orElseThrow());
    }
}
