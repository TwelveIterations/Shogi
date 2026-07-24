package net.blay09.mods.shogi.client.gui;

import net.blay09.mods.shogi.effect.EmptyEffect;
import net.blay09.mods.shogi.scope.internal.ShogiScopeImpl;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShogiEffectSuggestionProviderTest {
    @Test
    void suggestsDefaultNamespaceEffectsByPath() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        final var suggestions = provider.suggest("no", 2);

        assertEquals(new TextSuggestions(0, 2, List.of("noop")), suggestions);
    }

    @Test
    void suggestsQualifiedEffectsForNonDefaultNamespaces() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        final var suggestions = provider.suggest("test:", 5);

        assertEquals(new TextSuggestions(0, 5, List.of("test:custom")), suggestions);
    }

    @Test
    void doesNotSuggestWithoutFilterCharacters() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertNull(provider.suggest("", 0));
        assertNull(provider.suggest("noop ", 5));
        assertNull(provider.suggest("noop", 0));
    }

    @Test
    void doesNotSuggestInsideVariablesOrStrings() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertNull(provider.suggest("$no", 3));
        assertNull(provider.suggest("'no", 3));
    }

    private static ShogiScopeImpl createScope() {
        final var scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("shogi", "test"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        scope.registerEffect(Identifier.fromNamespaceAndPath("test", "custom"), EmptyEffect.MAP_CODEC);
        return scope;
    }
}
