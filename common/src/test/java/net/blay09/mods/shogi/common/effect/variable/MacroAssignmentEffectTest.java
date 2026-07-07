package net.blay09.mods.shogi.common.effect.variable;

import com.google.gson.JsonPrimitive;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.common.network.ShogiDefaultStreamCodecs;
import net.blay09.mods.shogi.context.internal.ShogiContextImpl;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroAssignmentEffectTest {

    @Test
    void storesEffectWithoutApplyingItUntilVariableIsApplied() {
        final var context = new ShogiContextImpl();
        final var applications = new AtomicInteger();
        final ShogiEffect<Boolean> macro = ShogiEffect.simple(
                Identifier.fromNamespaceAndPath("shogi", "test_macro"),
                _ -> {
                    applications.incrementAndGet();
                    return true;
                });

        new MacroAssignmentEffect("uses_xp", macro).apply(context);

        assertEquals(0, applications.get());

        final var effect = new ConditionEffect(
                new VariableEffect("uses_xp"),
                new AssignmentEffect("xp_points_cost", new ConstantEffect(new JsonPrimitive(123L))),
                EmptyEffect.INSTANCE);
        effect.apply(context);

        assertEquals(1, applications.get());
        assertEquals(new JsonPrimitive(123L), context.getVariable("xp_points_cost").orElseThrow());
    }

    @Test
    void doesNotExposeStoredEffectAsAggregatePayload() {
        ShogiDefaultStreamCodecs.registerDefaults();

        final var context = new ShogiContextImpl();
        final ShogiEffect<Boolean> macro = ShogiEffect.simple(
                Identifier.fromNamespaceAndPath("test", "unregistered_stream_codec"),
                _ -> true);

        final var aggregate = new AggregateEffect(List.of(new MacroAssignmentEffect("uses_xp", macro)));
        final var result = aggregate.apply(context);

        assertEquals(List.of(), result.left().orElseThrow());
        assertTrue(ShogiStreamCodecs.canEncodeEither(result));
    }
}
