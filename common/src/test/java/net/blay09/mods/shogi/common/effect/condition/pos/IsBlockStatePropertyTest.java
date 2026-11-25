package net.blay09.mods.shogi.common.effect.condition.pos;

import net.blay09.mods.shogi.context.MutableShogiContext;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsBlockStatePropertyTest {

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void matchesNamedPropertyValue() {
        final var state = Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.LIT, true);

        assertTrue(new IsBlockStateProperty("lit", "true").apply(MutableShogiContext.of(state)).left().orElseThrow());
        assertFalse(new IsBlockStateProperty("lit", "false").apply(MutableShogiContext.of(state)).left().orElseThrow());
    }

    @Test
    void ignoresMatchingValueOnAnotherProperty() {
        final var state = Blocks.REDSTONE_ORE.defaultBlockState().setValue(BlockStateProperties.LIT, true);

        assertFalse(new IsBlockStateProperty("missing", "true").apply(MutableShogiContext.of(state)).left().orElseThrow());
        final var waterlogged = Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        assertFalse(new IsBlockStateProperty("lit", "true").apply(MutableShogiContext.of(waterlogged)).left().orElseThrow());
    }

    @Test
    void rejectsWrongValueEvenWhenAnotherPropertyMatches() {
        final var state = Blocks.CAMPFIRE.defaultBlockState()
                .setValue(BlockStateProperties.LIT, false)
                .setValue(BlockStateProperties.WATERLOGGED, true);

        assertFalse(new IsBlockStateProperty("lit", "true").apply(MutableShogiContext.of(state)).left().orElseThrow());
    }

    @Test
    void preservesSerializedEnumAndNumericValues() {
        final var state = Blocks.FURNACE.defaultBlockState();
        assertTrue(new IsBlockStateProperty("facing", "north").apply(MutableShogiContext.of(state)).left().orElseThrow());
        assertTrue(new IsBlockStateProperty("age", "0")
                .apply(MutableShogiContext.of(Blocks.WHEAT.defaultBlockState())).left().orElseThrow());
    }
}
