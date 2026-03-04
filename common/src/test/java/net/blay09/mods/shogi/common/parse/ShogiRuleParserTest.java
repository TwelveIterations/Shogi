package net.blay09.mods.shogi.common.parse;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.effect.compose.AndEffect;
import net.blay09.mods.shogi.common.effect.compose.AnyEffect;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.common.effect.condition.pos.CanSeeSky;
import net.blay09.mods.shogi.common.effect.variable.AssignmentEffect;
import net.blay09.mods.shogi.common.effect.variable.BinaryOpEffect;
import net.blay09.mods.shogi.common.effect.variable.VariableEffect;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShogiRuleParserTest {

    @Test
    void parsesNumericLiteralAsConstant() {
        final var effect = parseOk(createScope(), "42");
        final var constant = assertInstanceOf(ConstantEffect.class, effect);
        assertEquals(new JsonPrimitive(42L), constant.value());
    }

    @Test
    void parsesStringLiteralAsConstant() {
        final var effect = parseOk(createScope(), "'hello'");
        final var constant = assertInstanceOf(ConstantEffect.class, effect);
        assertEquals(new JsonPrimitive("hello"), constant.value());
    }

    @Test
    void parsesBooleanLiteralAsConstant() {
        final var effect = parseOk(createScope(), "true");
        final var constant = assertInstanceOf(ConstantEffect.class, effect);
        assertEquals(new JsonPrimitive(true), constant.value());
    }

    @Test
    void parsesVariablePath() {
        final var effect = parseOk(createScope(), "$foo.bar");
        final var variable = assertInstanceOf(VariableEffect.class, effect);
        assertEquals("foo.bar", variable.name());
    }

    @Test
    void parsesAssignment() {
        final var effect = parseOk(createScope(), "$result = 5");
        final var assignment = assertInstanceOf(AssignmentEffect.class, effect);
        assertEquals("result", assignment.variable());
        final var value = assertInstanceOf(ConstantEffect.class, assignment.value());
        assertEquals(new JsonPrimitive(5L), value.value());
    }

    @Test
    void parsesConditionalAssignmentWithNamedArgsOutOfOrder() {
        final var effect = parseOk(createScope(), "$xp_cost = if(condition = can_see_sky, else = 27, then = $distance * 0.01)");
        final var assignment = assertInstanceOf(AssignmentEffect.class, effect);
        assertEquals("xp_cost", assignment.variable());

        final var condition = assertInstanceOf(ConditionEffect.class, assignment.value());
        assertInstanceOf(CanSeeSky.class, condition.condition());

        final var thenEffect = assertInstanceOf(BinaryOpEffect.class, condition.trueEffect());
        assertEquals("*", thenEffect.op());
        assertEquals("distance", assertInstanceOf(VariableEffect.class, thenEffect.left()).name());
        assertEquals(new JsonPrimitive(0.01), assertInstanceOf(ConstantEffect.class, thenEffect.right()).value());

        final var elseEffect = assertInstanceOf(ConstantEffect.class, condition.falseEffect());
        assertEquals(new JsonPrimitive(27L), elseEffect.value());
    }

    @Test
    void parsesOperatorPrecedence() {
        final var effect = parseOk(createScope(), "1 + 2 * 3");
        final var root = assertInstanceOf(BinaryOpEffect.class, effect);
        assertEquals("+", root.op());

        final var left = assertInstanceOf(ConstantEffect.class, root.left());
        assertEquals(new JsonPrimitive(1L), left.value());

        final var right = assertInstanceOf(BinaryOpEffect.class, root.right());
        assertEquals("*", right.op());
        assertEquals(new JsonPrimitive(2L), assertInstanceOf(ConstantEffect.class, right.left()).value());
        assertEquals(new JsonPrimitive(3L), assertInstanceOf(ConstantEffect.class, right.right()).value());
    }

    @Test
    void parsesOperatorParentheses() {
        final var effect = parseOk(createScope(), "(1 + 2) * 3");
        final var root = assertInstanceOf(BinaryOpEffect.class, effect);
        assertEquals("*", root.op());

        final var left = assertInstanceOf(BinaryOpEffect.class, root.left());
        assertEquals("+", left.op());
        assertEquals(new JsonPrimitive(1L), assertInstanceOf(ConstantEffect.class, left.left()).value());
        assertEquals(new JsonPrimitive(2L), assertInstanceOf(ConstantEffect.class, left.right()).value());

        assertEquals(new JsonPrimitive(3L), assertInstanceOf(ConstantEffect.class, root.right()).value());
    }

    @Test
    void parsesPositionalFunctionArgumentsUsingOrdinals() {
        final var effect = parseOk(createScope(), "binary_op('+', 1, 2)");
        final var binary = assertInstanceOf(BinaryOpEffect.class, effect);
        assertEquals("+", binary.op());
        assertEquals(new JsonPrimitive(1L), assertInstanceOf(ConstantEffect.class, binary.left()).value());
        assertEquals(new JsonPrimitive(2L), assertInstanceOf(ConstantEffect.class, binary.right()).value());
    }

    @Test
    void failsForMixedNamedAndPositionalArguments() {
        assertContains(parseErr(createScope(), "binary_op(op='+', 1, 2)"), "Cannot mix named and positional arguments");
    }

    @Test
    void parsesVariadicAndFromPositionalArguments() {
        final var effect = parseOk(createScope(), "and(noop, noop, noop)");
        final var and = assertInstanceOf(AndEffect.class, effect);
        assertEquals(3, and.conditions().size());
        and.conditions().forEach(condition -> assertInstanceOf(EmptyEffect.class, condition));
    }

    @Test
    void parsesVariadicAggregateFromPositionalArguments() {
        final var effect = parseOk(createScope(), "aggregate(noop, noop)");
        final var aggregate = assertInstanceOf(AggregateEffect.class, effect);
        assertEquals(2, aggregate.effects().size());
        aggregate.effects().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
    }

    @Test
    void parsesSingleConditionArrow() {
        final var effect = parseOk(createScope(), "noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        assertInstanceOf(EmptyEffect.class, condition.condition());
        assertInstanceOf(EmptyEffect.class, condition.trueEffect());
        assertInstanceOf(EmptyEffect.class, condition.falseEffect());
    }

    @Test
    void parsesMultipleConditionsAsAnd() {
        final var effect = parseOk(createScope(), "noop + noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var and = assertInstanceOf(AndEffect.class, condition.condition());
        assertEquals(2, and.conditions().size());
        and.conditions().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
        assertInstanceOf(EmptyEffect.class, condition.trueEffect());
        assertInstanceOf(EmptyEffect.class, condition.falseEffect());
    }

    @Test
    void parsesMultipleConditionsAsAny() {
        final var effect = parseOk(createScope(), "noop, noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var any = assertInstanceOf(AnyEffect.class, condition.condition());
        assertEquals(2, any.conditions().size());
        any.conditions().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
        assertInstanceOf(EmptyEffect.class, condition.trueEffect());
        assertInstanceOf(EmptyEffect.class, condition.falseEffect());
    }

    @Test
    void parsesMixedConditionOperatorsWithAndPrecedence() {
        final var effect = parseOk(createScope(), "noop + noop, noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var any = assertInstanceOf(AnyEffect.class, condition.condition());
        assertEquals(2, any.conditions().size());

        final var and = assertInstanceOf(AndEffect.class, any.conditions().get(0));
        assertEquals(2, and.conditions().size());
        assertInstanceOf(EmptyEffect.class, any.conditions().get(1));
        assertInstanceOf(EmptyEffect.class, condition.trueEffect());
        assertInstanceOf(EmptyEffect.class, condition.falseEffect());
    }

    @Test
    void parsesMixedConditionOperatorsWithAndPrecedenceOnRightBranch() {
        final var effect = parseOk(createScope(), "noop, noop + noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var any = assertInstanceOf(AnyEffect.class, condition.condition());
        assertEquals(2, any.conditions().size());

        assertInstanceOf(EmptyEffect.class, any.conditions().get(0));
        final var and = assertInstanceOf(AndEffect.class, any.conditions().get(1));
        assertEquals(2, and.conditions().size());
        assertInstanceOf(EmptyEffect.class, condition.trueEffect());
        assertInstanceOf(EmptyEffect.class, condition.falseEffect());
    }

    @Test
    void parsesConditionOperatorsWithoutWhitespace() {
        final var effect = parseOk(createScope(), "noop+noop,noop->noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var any = assertInstanceOf(AnyEffect.class, condition.condition());
        assertEquals(2, any.conditions().size());

        final var and = assertInstanceOf(AndEffect.class, any.conditions().get(0));
        assertEquals(2, and.conditions().size());
        assertInstanceOf(EmptyEffect.class, any.conditions().get(1));
    }

    @Test
    void parsesParenthesizedConditionGroup() {
        final var effect = parseOk(createScope(), "noop + (noop, noop) -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var and = assertInstanceOf(AndEffect.class, condition.condition());
        assertEquals(2, and.conditions().size());
        assertInstanceOf(EmptyEffect.class, and.conditions().get(0));

        final var any = assertInstanceOf(AnyEffect.class, and.conditions().get(1));
        assertEquals(2, any.conditions().size());
        any.conditions().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
    }

    @Test
    void parsesParenthesizedConditionGroupOnLeftBranch() {
        final var effect = parseOk(createScope(), "(noop, noop) + noop -> noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var and = assertInstanceOf(AndEffect.class, condition.condition());
        assertEquals(2, and.conditions().size());

        final var any = assertInstanceOf(AnyEffect.class, and.conditions().getFirst());
        assertEquals(2, any.conditions().size());
        any.conditions().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
        assertInstanceOf(EmptyEffect.class, and.conditions().get(1));
    }

    @Test
    void parsesNestedParenthesizedConditionGroupsWithoutWhitespace() {
        final var effect = parseOk(createScope(), "noop+((noop,noop)+noop)->noop");
        final var condition = assertInstanceOf(ConditionEffect.class, effect);
        final var rootAnd = assertInstanceOf(AndEffect.class, condition.condition());
        assertEquals(2, rootAnd.conditions().size());

        final var nestedAnd = assertInstanceOf(AndEffect.class, rootAnd.conditions().get(1));
        assertEquals(2, nestedAnd.conditions().size());
        assertInstanceOf(EmptyEffect.class, nestedAnd.conditions().get(1));

        final var nestedAny = assertInstanceOf(AnyEffect.class, nestedAnd.conditions().get(0));
        assertEquals(2, nestedAny.conditions().size());
        nestedAny.conditions().forEach(rule -> assertInstanceOf(EmptyEffect.class, rule));
    }

    @Test
    void parsesBareCall() {
        final var effect = parseOk(createScope(), "noop");
        assertInstanceOf(EmptyEffect.class, effect);
    }

    @Test
    void usesProvidedDefaultNamespaceForUnqualifiedIdentifier() {
        final var scope = createScopeWithoutNoop();
        scope.registerEffect(Identifier.fromNamespaceAndPath("custom", "foo"), EmptyEffect.MAP_CODEC);
        final var effect = parseOk(scope, "foo", "custom");
        assertInstanceOf(EmptyEffect.class, effect);
    }

    @Test
    void usesFirstMatchingDefaultNamespaceForUnqualifiedIdentifier() {
        final var scope = createScopeWithoutNoop();
        scope.registerEffect(WaystonesFooEffect.IDENTIFIER, WaystonesFooEffect.MAP_CODEC);
        scope.registerEffect(ShogiFooEffect.IDENTIFIER, ShogiFooEffect.MAP_CODEC);
        scope.setDefaultNamespaces(List.of("waystones", "shogi"));

        final var effect = parseOk(scope, "foo");
        assertInstanceOf(WaystonesFooEffect.class, effect);
    }

    @Test
    void fallsBackToLaterDefaultNamespaceForUnqualifiedIdentifier() {
        final var scope = createScopeWithoutNoop();
        scope.registerEffect(ShogiFooEffect.IDENTIFIER, ShogiFooEffect.MAP_CODEC);
        scope.setDefaultNamespaces(List.of("waystones", "shogi"));

        final var effect = parseOk(scope, "foo");
        assertInstanceOf(ShogiFooEffect.class, effect);
    }

    @Test
    void failsForDuplicateNamedParameter() {
        assertContains(parseErr(createScope(), "binary_op(op='+', op='-', left=1, right=2)"), "Duplicate named parameter");
    }

    @Test
    void failsForTooManyPositionalArguments() {
        assertContains(parseErr(createScope(), "binary_op('+', 1, 2, 3)"), "Too many positional arguments");
    }

    @Test
    void failsForPositionalArgumentsWhenUnsupported() {
        assertContains(parseErr(createScope(), "noop(1)"), "does not support positional parameters");
    }

    @Test
    void failsForInvalidEffectIdentifier() {
        assertContains(parseErr(createScope(), "Bad()"), "Invalid effect identifier: Bad");
    }

    @Test
    void failsForTrailingTokens() {
        assertContains(parseErr(createScope(), "1 2"), "Unexpected trailing token");
    }

    @Test
    void failsForMalformedCallSyntax() {
        assertContains(parseErr(createScope(), "binary_op('+', 1, 2"), "Expected ','");
    }

    @Test
    void failsForMissingConditionGroupClosingParenthesis() {
        assertContains(parseErr(createScope(), "noop + (noop, noop -> noop) -> noop"), "Expected ')'");
    }

    @Test
    void failsForEmptyConditionGroup() {
        assertContains(parseErr(createScope(), "() -> noop"), "Expected condition");
    }

    @Test
    void failsForEmptyConditionGroupAfterAnd() {
        assertContains(parseErr(createScope(), "noop + () -> noop"), "Expected condition");
    }

    @Test
    void failsForUnknownEffectAtCodecStage() {
        assertContains(parseErr(createScope(), "not_registered()"), "Unknown effect");
    }

    private static ShogiScope createScope() {
        final ShogiScopeImpl scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("shogi", "test"));
        registerParserEffects(scope, true);
        return scope;
    }

    private static ShogiScope createScopeWithoutNoop() {
        final ShogiScopeImpl scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("shogi", "test"));
        registerParserEffects(scope, false);
        return scope;
    }

    private static void registerParserEffects(ShogiScope scope, boolean includeNoop) {
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        if (includeNoop) {
            scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        }
        scope.registerEffect(AggregateEffect.IDENTIFIER, AggregateEffect.mapCodec(scope), List.of("effects"));
        scope.registerEffect(ConditionEffect.IDENTIFIER, ConditionEffect.mapCodec(scope), List.of("condition", "then", "else"));
        scope.registerEffect(AndEffect.IDENTIFIER, AndEffect.mapCodec(scope), List.of("conditions"));
        scope.registerEffect(AnyEffect.IDENTIFIER, AnyEffect.mapCodec(scope), List.of("conditions"));
        scope.registerEffect(CanSeeSky.IDENTIFIER, CanSeeSky.MAP_CODEC);
        scope.registerEffect(VariableEffect.IDENTIFIER, VariableEffect.MAP_CODEC, List.of("path"));
        scope.registerEffect(AssignmentEffect.IDENTIFIER, AssignmentEffect.mapCodec(scope), List.of("variable", "value"));
        scope.registerEffect(BinaryOpEffect.IDENTIFIER, BinaryOpEffect.mapCodec(scope), List.of("op", "left", "right"));
    }

    private static ShogiEffect<?> parseOk(ShogiScope scope, String input) {
        final DataResult<ShogiEffect<?>> result = ShogiRuleParser.parse(scope, input);
        return result.result().orElseThrow(() -> new AssertionError("Expected parse success, got error: " + parseError(result)));
    }

    private static ShogiEffect<?> parseOk(ShogiScope scope, String input, String defaultNamespace) {
        final DataResult<ShogiEffect<?>> result = ShogiRuleParser.parse(scope, input, defaultNamespace);
        return result.result().orElseThrow(() -> new AssertionError("Expected parse success, got error: " + parseError(result)));
    }

    private static String parseErr(ShogiScope scope, String input) {
        final DataResult<ShogiEffect<?>> result = ShogiRuleParser.parse(scope, input);
        if (result.result().isPresent()) {
            throw new AssertionError("Expected parse error, got success");
        }
        return parseError(result);
    }

    private static String parseError(DataResult<?> result) {
        return result.error()
                .map(Object::toString)
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .orElse("<missing error>");
    }

    private record WaystonesFooEffect() implements ShogiEffect<Object> {
        private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("waystones", "foo");
        private static final MapCodec<WaystonesFooEffect> MAP_CODEC = MapCodec.unit(new WaystonesFooEffect());

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<Object, Object> apply(net.blay09.mods.shogi.context.ShogiContext context) {
            return Either.left(true);
        }
    }

    private record ShogiFooEffect() implements ShogiEffect<Object> {
        private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "foo");
        private static final MapCodec<ShogiFooEffect> MAP_CODEC = MapCodec.unit(new ShogiFooEffect());

        @Override
        public Identifier identifier() {
            return IDENTIFIER;
        }

        @Override
        public Either<Object, Object> apply(net.blay09.mods.shogi.context.ShogiContext context) {
            return Either.left(true);
        }
    }

    private static void assertContains(String message, String expectedFragment) {
        assertTrue(Objects.requireNonNull(message).contains(expectedFragment),
                () -> "Expected error message to contain [" + expectedFragment + "], but was [" + message + "]");
    }
}
