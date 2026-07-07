package net.blay09.mods.shogi.common;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blay09.mods.shogi.common.effect.compose.AggregateEffect;
import net.blay09.mods.shogi.common.effect.compose.ConditionEffect;
import net.blay09.mods.shogi.common.effect.variable.AssignmentEffect;
import net.blay09.mods.shogi.common.effect.variable.BinaryOpEffect;
import net.blay09.mods.shogi.common.effect.variable.HasValueEffect;
import net.blay09.mods.shogi.common.effect.variable.VariableEffect;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class CachedShogiRuleTest {

    @Test
    void compilesSingleStringRule() {
        final var resolver = CachedShogiRule.ofRule(
                createScope(),
                () -> "42"
        );

        final var effect = resolver.get(RegistryAccess.EMPTY);
        final var constant = assertInstanceOf(ConstantEffect.class, effect);
        assertEquals(new JsonPrimitive(42L), constant.value());
    }

    @Test
    void compilesStringListIntoAggregate() {
        final var resolver = CachedShogiRule.ofRules(
                createScope(),
                () -> List.of("$xp_points_cost = 12", "$xp_points_cost * 2")
        );

        final var effect = resolver.get(RegistryAccess.EMPTY);
        final var aggregate = assertInstanceOf(AggregateEffect.class, effect);
        assertEquals(3, aggregate.effects().size());
        assertInstanceOf(AssignmentEffect.class, aggregate.effects().getFirst());
        final var guardedAutoApplied = assertInstanceOf(ConditionEffect.class, aggregate.effects().get(2));
        assertInstanceOf(HasValueEffect.class, guardedAutoApplied.condition());
        assertInstanceOf(XpPointsCostEffect.class, guardedAutoApplied.trueEffect());
    }

    @Test
    void skipsInvalidListEntriesWhileKeepingValidSiblings() {
        final var resolver = CachedShogiRule.ofRules(
                createScope(),
                () -> List.of("not valid(", "42")
        );

        final var effect = resolver.get(RegistryAccess.EMPTY);
        final var aggregate = assertInstanceOf(AggregateEffect.class, effect);
        assertEquals(1, aggregate.effects().size());
        final var constant = assertInstanceOf(ConstantEffect.class, aggregate.effects().getFirst());
        assertEquals(new JsonPrimitive(42L), constant.value());
    }

    @Test
    void cachesCompiledEffectUntilInvalidated() {
        final AtomicReference<String> rule = new AtomicReference<>("41");
        final var resolver = CachedShogiRule.ofRule(
                createScope(),
                rule::get
        );

        final var initialEffect = resolver.get(RegistryAccess.EMPTY);
        rule.set("42");

        final var cachedEffect = resolver.get(RegistryAccess.EMPTY);
        assertSame(initialEffect, cachedEffect);

        resolver.invalidate();

        final var rebuiltEffect = resolver.get(RegistryAccess.EMPTY);
        assertNotSame(initialEffect, rebuiltEffect);
        final var constant = assertInstanceOf(ConstantEffect.class, rebuiltEffect);
        assertEquals(new JsonPrimitive(42L), constant.value());
    }

    @Test
    void fallsBackToNoopForInvalidSingleRule() {
        final var resolver = CachedShogiRule.ofRule(
                createScope(),
                () -> "not valid("
        );

        assertSame(EmptyEffect.INSTANCE, resolver.get(RegistryAccess.EMPTY));
    }

    private static ShogiScope createScope() {
        final ShogiScopeImpl scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("shogi", "test"));
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        scope.registerEffect(VariableEffect.IDENTIFIER, VariableEffect.MAP_CODEC, List.of("name"));
        scope.registerEffect(HasValueEffect.IDENTIFIER, HasValueEffect.MAP_CODEC, List.of("variable"));
        scope.registerEffect(AssignmentEffect.IDENTIFIER, AssignmentEffect.mapCodec(scope), List.of("variable", "value"));
        scope.registerEffect(BinaryOpEffect.IDENTIFIER, BinaryOpEffect.mapCodec(scope), List.of("op", "left", "right"));
        scope.registerEffect(XpPointsCostEffect.IDENTIFIER, XpPointsCostEffect.mapCodec(scope), List.of("value"));
        return scope;
    }

    private record XpPointsCostEffect(ShogiEffect<?> value) implements ShogiEffect<Object> {
        private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("shogi", "xp_points_cost");

        private static MapCodec<XpPointsCostEffect> mapCodec(ShogiScope scope) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    scope.getEffectCodec().fieldOf("value").forGetter(XpPointsCostEffect::value)
            ).apply(instance, XpPointsCostEffect::new));
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
