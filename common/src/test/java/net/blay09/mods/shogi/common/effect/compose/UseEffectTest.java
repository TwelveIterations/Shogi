package net.blay09.mods.shogi.common.effect.compose;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import net.blay09.mods.shogi.common.scope.ShogiOverrideProviderImpl;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepositories;
import net.blay09.mods.shogi.common.scope.ShogiRuleRepository;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.effect.ConstantEffect;
import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.effect.ShogiEmpty;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseEffectTest {

    @Test
    void prefersValueOverrideBeforeDatapackRule() {
        final var scope = createScope();
        final var target = Identifier.fromNamespaceAndPath("test", "target");
        repository(scope).apply(
                Map.of(target, new ConstantEffect(new JsonPrimitive(1))),
                Map.of(target, new ConstantEffect(new JsonPrimitive(2)))
        );

        final var result = new UseEffect(scope, target).apply(MutableShogiContext.create());
        assertEquals(new JsonPrimitive(1), result.left().orElseThrow());
    }

    @Test
    void fallsBackToDatapackRuleWhenNoValueOverrideExists() {
        final var scope = createScope();
        final var target = Identifier.fromNamespaceAndPath("test", "target");
        repository(scope).apply(
                Map.of(),
                Map.of(target, new ConstantEffect(new JsonPrimitive(2)))
        );

        final var result = new UseEffect(scope, target).apply(MutableShogiContext.create());
        assertEquals(new JsonPrimitive(2), result.left().orElseThrow());
    }

    @Test
    void returnsEmptyWhenImportTargetIsMissing() {
        final var scope = createScope();
        final var result = new UseEffect(scope, Identifier.fromNamespaceAndPath("test", "missing")).apply(MutableShogiContext.create());
        assertTrue(result.right().isPresent());
        assertInstanceOf(ShogiEmpty.class, result.right().orElseThrow());
    }

    @Test
    void returnsEmptyOnCyclicImports() {
        final var scope = createScope();
        final var first = Identifier.fromNamespaceAndPath("test", "first");
        final var second = Identifier.fromNamespaceAndPath("test", "second");
        repository(scope).apply(
                Map.of(),
                Map.of(
                        first, new UseEffect(scope, second),
                        second, new UseEffect(scope, first)
                )
        );

        final Either<Object, ?> result = new UseEffect(scope, first).apply(MutableShogiContext.create());
        assertTrue(result.right().isPresent());
        assertInstanceOf(ShogiEmpty.class, result.right().orElseThrow());
    }

    private static ShogiScopeImpl createScope() {
        final var scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("test", "scope"));
        scope.registerEffect(ConstantEffect.IDENTIFIER, ConstantEffect.MAP_CODEC, java.util.List.of("value"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        scope.registerEffect(UseEffect.IDENTIFIER, UseEffect.mapCodec(scope), java.util.List.of("identifier"));
        final var repository = new ShogiRuleRepository();
        scope.registerOverrideProvider(new ShogiOverrideProviderImpl(repository));
        ShogiRuleRepositories.register(scope, repository);
        return scope;
    }

    private static ShogiRuleRepository repository(ShogiScopeImpl scope) {
        return ShogiRuleRepositories.get(scope).orElseThrow();
    }
}
