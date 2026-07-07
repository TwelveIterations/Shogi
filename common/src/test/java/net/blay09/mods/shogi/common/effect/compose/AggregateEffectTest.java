package net.blay09.mods.shogi.common.effect.compose;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.common.effect.cost.ExperiencePointsCost;
import net.blay09.mods.shogi.common.effect.variable.AssignmentEffect;
import net.blay09.mods.shogi.common.effect.variable.BinaryOpEffect;
import net.blay09.mods.shogi.common.effect.variable.HasValueEffect;
import net.blay09.mods.shogi.common.effect.variable.VariableEffect;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.internal.ShogiContextImpl;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateEffectTest {

    @Test
    void autoAppliesSingleParameterEffectFromAssignedVariable() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        final List<ShogiEffect<?>> effects = List.of(
                parseOk(scope, "$xp_points_cost = 12"),
                parseOk(scope, "$xp_points_cost * 2")
        );

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(3, aggregate.effects().size());

        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(2));
        final var condition = assertInstanceOf(HasValueEffect.class, guardedAutoApplied.condition());
        assertEquals("xp_points_cost", condition.variable());

        final var autoApplied = assertInstanceOf(ExperiencePointsCost.class, guardedAutoApplied.trueEffect());
        final var value = assertInstanceOf(VariableEffect.class, autoApplied.xp());
        assertEquals("xp_points_cost", value.name());
    }

    @Test
    void autoAppliesSingleParameterEffectFromNestedAssignedVariable() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        final List<ShogiEffect<?>> effects = List.of(
                parseOk(scope, "$uses_xp -> $xp_points_cost = 12")
        );

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(2, aggregate.effects().size());

        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(1));
        final var condition = assertInstanceOf(HasValueEffect.class, guardedAutoApplied.condition());
        assertEquals("xp_points_cost", condition.variable());

        final var autoApplied = assertInstanceOf(ExperiencePointsCost.class, guardedAutoApplied.trueEffect());
        final var value = assertInstanceOf(VariableEffect.class, autoApplied.xp());
        assertEquals("xp_points_cost", value.name());
    }

    @Test
    void guardedAutoAppliedEffectSkipsMissingNestedAssignment() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        final List<ShogiEffect<?>> effects = List.of(
                parseOk(scope, "$uses_xp -> $xp_points_cost = 12")
        );

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        final var result = aggregate.apply(new ShogiContextImpl());

        assertTrue(result.left().isPresent());
        assertTrue(result.left().orElseThrow().isEmpty());
    }

    @Test
    void doesNotAutoApplyForMultiParameterEffects() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        final List<ShogiEffect<?>> effects = List.of(parseOk(scope, "$binary_op = 12"));

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(1, aggregate.effects().size());
    }

    @Test
    void usesScopeNamespaceForUnqualifiedVariableNames() {
        final var customScope = createScope(Identifier.fromNamespaceAndPath("custom", "test"), true);
        final List<ShogiEffect<?>> effects = List.of(parseOk(customScope, "$xp_points_cost = 12"));

        final var aggregate = AggregateEffect.withAutoApplied(customScope, JsonOps.INSTANCE, effects);
        assertEquals(2, aggregate.effects().size());
        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(1));
        assertInstanceOf(CustomScopeUnaryEffect.class, guardedAutoApplied.trueEffect());
    }

    @Test
    void usesOrderedDefaultNamespacesForAutoApply() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), true);
        scope.setDefaultNamespaces(List.of("custom", "shogi"));
        final List<ShogiEffect<?>> effects = List.of(parseOk(scope, "$xp_points_cost = 12"));

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(2, aggregate.effects().size());
        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(1));
        assertInstanceOf(CustomScopeUnaryEffect.class, guardedAutoApplied.trueEffect());
    }

    @Test
    void usesFirstAutoApplicableMatchAcrossDefaultNamespaces() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        scope.registerEffect(CustomScopeBinaryEffect.IDENTIFIER, CustomScopeBinaryEffect.mapCodec(scope), List.of("left", "right"));
        scope.setDefaultNamespaces(List.of("custom", "shogi"));
        final List<ShogiEffect<?>> effects = List.of(parseOk(scope, "$xp_points_cost = 12"));

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(2, aggregate.effects().size());
        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(1));
        assertInstanceOf(ExperiencePointsCost.class, guardedAutoApplied.trueEffect());
    }

    @Test
    void skipsAutoApplyForInvalidVariablePaths() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);
        final List<ShogiEffect<?>> effects = List.of(parseOk(scope, "$XP_Points_Cost = 12"));

        final var aggregate = AggregateEffect.withAutoApplied(scope, JsonOps.INSTANCE, effects);
        assertEquals(1, aggregate.effects().size());
        assertInstanceOf(AssignmentEffect.class, aggregate.effects().getFirst());
    }

    @Test
    void codecDoesNotAutoApplyForExplicitAggregateJson() {
        final var scope = createScope(Identifier.fromNamespaceAndPath("shogi", "test"), false);

        final var assignment = parseOk(scope, "$xp_points_cost = 12");
        final var computed = parseOk(scope, "$xp_points_cost * 2");

        final var effectsJson = new JsonArray();
        effectsJson.add(scope.getEffectCodec().encodeStart(JsonOps.INSTANCE, assignment).result().orElseThrow());
        effectsJson.add(scope.getEffectCodec().encodeStart(JsonOps.INSTANCE, computed).result().orElseThrow());

        final var aggregateJson = new JsonObject();
        aggregateJson.addProperty("type", "shogi:aggregate");
        aggregateJson.add("effects", effectsJson);

        final var decoded = scope.getEffectCodec().parse(JsonOps.INSTANCE, aggregateJson).result().orElseThrow();
        final var aggregate = assertInstanceOf(AggregateEffect.class, decoded);
        assertEquals(2, aggregate.effects().size());
    }

    private static ShogiScope createScope(Identifier scopeIdentifier, boolean includeCustomNamespaceUnary) {
        final ShogiScopeImpl scope = new ShogiScopeImpl(scopeIdentifier);
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        scope.registerEffect(VariableEffect.IDENTIFIER, VariableEffect.MAP_CODEC, List.of("name"));
        scope.registerEffect(HasValueEffect.IDENTIFIER, HasValueEffect.MAP_CODEC, List.of("variable"));
        scope.registerEffect(AssignmentEffect.IDENTIFIER, AssignmentEffect.mapCodec(scope), List.of("variable", "value"));
        scope.registerEffect(BinaryOpEffect.IDENTIFIER, BinaryOpEffect.mapCodec(scope), List.of("op", "left", "right"));
        scope.registerEffect(AggregateEffect.IDENTIFIER, AggregateEffect.mapCodec(scope), List.of("effects"));
        scope.registerEffect(ConditionEffect.IDENTIFIER, ConditionEffect.mapCodec(scope), List.of("condition", "then", "else"));
        scope.registerEffect(ExperiencePointsCost.IDENTIFIER, ExperiencePointsCost.mapCodec(scope), List.of("xp"));
        if (includeCustomNamespaceUnary) {
            scope.registerEffect(CustomScopeUnaryEffect.IDENTIFIER, CustomScopeUnaryEffect.mapCodec(scope), List.of("value"));
        }
        return scope;
    }

    private static ShogiEffect<?> parseOk(ShogiScope scope, String input) {
        final DataResult<ShogiEffect<?>> result = ShogiRuleParser.parse(scope, JsonOps.INSTANCE, input);
        return result.result().orElseThrow(() -> new AssertionError("Expected parse success, got error: " + parseError(result)));
    }

    private static String parseError(DataResult<?> result) {
        return result.error()
                .map(Object::toString)
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .orElse("<missing error>");
    }

    private record CustomScopeUnaryEffect(ShogiEffect<?> value) implements ShogiEffect<Object> {
        private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("custom", "xp_points_cost");

        private static MapCodec<CustomScopeUnaryEffect> mapCodec(ShogiScope scope) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    scope.getEffectCodec().fieldOf("value").forGetter(CustomScopeUnaryEffect::value)
            ).apply(instance, CustomScopeUnaryEffect::new));
        }

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<Object, Object> apply(ShogiContext context) {
            return Either.left(true);
        }
    }

    private record CustomScopeBinaryEffect(ShogiEffect<?> left, ShogiEffect<?> right) implements ShogiEffect<Object> {
        private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("custom", "xp_points_cost");

        private static MapCodec<CustomScopeBinaryEffect> mapCodec(ShogiScope scope) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    scope.getEffectCodec().fieldOf("left").forGetter(CustomScopeBinaryEffect::left),
                    scope.getEffectCodec().fieldOf("right").forGetter(CustomScopeBinaryEffect::right)
            ).apply(instance, CustomScopeBinaryEffect::new));
        }

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<Object, Object> apply(ShogiContext context) {
            return Either.left(true);
        }
    }
}
