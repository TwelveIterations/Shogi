package net.blay09.mods.shogi.common.effect.context.pos;

import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionModifierEffectsTest {

    private static final Identifier CAPTURE_POS = Identifier.fromNamespaceAndPath("shogi", "capture_pos");
    private static final ShogiEffect<BlockPos> CAPTURE_POS_EFFECT = ShogiEffect.simple(CAPTURE_POS, ShogiContext::requireBlockPos);

    @Test
    void aboveChangesNestedContextPosition() {
        final var originalPos = new BlockPos(4, 10, -2);
        final var context = MutableShogiContext.create().withBlockPos(originalPos);

        final var result = new Above<>(CAPTURE_POS_EFFECT).apply(context);

        assertEquals(originalPos.above(), result.left().orElseThrow());
        assertEquals(originalPos, context.blockPos());
    }

    @Test
    void belowChangesNestedContextPosition() {
        final var originalPos = new BlockPos(4, 10, -2);
        final var context = MutableShogiContext.create().withBlockPos(originalPos);

        final var result = new Below<>(CAPTURE_POS_EFFECT).apply(context);

        assertEquals(originalPos.below(), result.left().orElseThrow());
        assertEquals(originalPos, context.blockPos());
    }
}
