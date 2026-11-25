package net.blay09.mods.shogi.common.effect.compose;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.blay09.mods.shogi.common.parse.ShogiRuleParser;
import net.blay09.mods.shogi.common.effect.variable.BinaryOpEffect;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.internal.MinimalShogiFactories;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEffectTest {
    private static final Identifier VALUE = id("value");

    @Test
    void evaluatesParsedExpressionWithOriginalTypedInput() {
        final var scope = scope();
        override(scope, Map.of(VALUE, parse(scope, "default() * 2")));
        final var calls = new AtomicInteger();
        final var value = new MinimalShogiFactories().<String, Integer>value(VALUE, scope, input -> {
            calls.incrementAndGet();
            return input.length();
        });
        assertEquals(6f, value.get("abc").left().orElseThrow());
        assertEquals(1, calls.get());
    }

    @Test
    void nestedResolutionKeepsFallbacksSeparateAndDoesNotLeakToInput() {
        final var scope = scope();
        final var inner = id("inner");
        final var input = MutableShogiContext.create();
        override(scope, Map.of(
                inner, DefaultEffect.INSTANCE,
                VALUE, ShogiEffect.simple(id("nested"), context -> {
                    assertEquals(7, scope.resolve(inner, context, ignored -> Either.left(7)).left().orElseThrow());
                    return DefaultEffect.INSTANCE.apply(context.fork()).left().orElseThrow();
                })
        ));
        assertEquals(3, scope.resolve(VALUE, input, ignored -> Either.left(3)).left().orElseThrow());
        assertInstanceOf(IllegalStateException.class, DefaultEffect.INSTANCE.apply(input).right().orElseThrow());
    }

    @Test
    void importedOverrideUsesCallingValuesFallback() {
        final var scope = scope();
        final var imported = id("imported");
        override(scope, Map.of(VALUE, new UseEffect(scope, imported), imported, DefaultEffect.INSTANCE));
        assertEquals(9, scope.resolve(VALUE, "input", ignored -> Either.left(9)).left().orElseThrow());
    }

    @Test
    void propagatesProviderFailuresAndExceptions() {
        final var scope = scope();
        override(scope, Map.of(VALUE, parse(scope, "default() * 2")));
        final var failure = new Object();
        assertSame(failure, scope.resolve(VALUE, "input", ignored -> Either.right(failure)).right().orElseThrow());
        final var exception = new IllegalArgumentException("provider failure");
        assertSame(exception, scope.resolve(VALUE, "input", ignored -> { throw exception; }).right().orElseThrow());
    }

    @Test
    void evaluatesEachInvocationAndStillSupportsImplicitFallback() {
        final var scope = scope();
        override(scope, Map.of(VALUE, parse(scope, "default() + default()")));
        final var calls = new AtomicInteger();
        assertEquals(3f, scope.resolve(VALUE, "input", ignored -> Either.left(calls.incrementAndGet())).left().orElseThrow());
        assertEquals(2, calls.get());
        final var emptyScope = scope();
        override(emptyScope, Map.of(VALUE, EmptyEffect.INSTANCE));
        assertEquals(4, emptyScope.resolve(VALUE, "input", ignored -> Either.left(4)).left().orElseThrow());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static ShogiScopeImpl scope() {
        final var scope = new ShogiScopeImpl(id("scope"));
        scope.setDefaultNamespaces(List.of("shogi"));
        scope.registerEffect(DefaultEffect.IDENTIFIER, DefaultEffect.MAP_CODEC);
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, List.of("value"));
        scope.registerEffect(BinaryOpEffect.IDENTIFIER, BinaryOpEffect.mapCodec(scope), List.of("op", "left", "right"));
        return scope;
    }

    private static void override(ShogiScopeImpl scope, Map<Identifier, ShogiEffect<?>> overrides) {
        scope.registerOverrideProvider(identifier -> Optional.ofNullable(overrides.get(identifier)));
    }

    private static ShogiEffect<?> parse(ShogiScopeImpl scope, String expression) {
        return ShogiRuleParser.parse(scope, JsonOps.INSTANCE, expression).getOrThrow();
    }
}
