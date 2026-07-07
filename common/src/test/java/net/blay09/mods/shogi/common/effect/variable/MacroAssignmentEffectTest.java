package net.blay09.mods.shogi.common.effect.variable;

import com.google.gson.JsonPrimitive;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.context.internal.ShogiContextImpl;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
