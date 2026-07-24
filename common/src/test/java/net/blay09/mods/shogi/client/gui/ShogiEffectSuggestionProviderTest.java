package net.blay09.mods.shogi.client.gui;

import net.blay09.mods.shogi.common.util.ShogiEffectSuggestionProvider;
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
    void keepsExactMatchWhenOtherSuggestionsSharePrefix() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        final var suggestions = provider.suggest("is_near", 7);

        assertEquals(new TextSuggestions(0, 7, List.of("is_near", "is_near_block_entity")), suggestions);
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

    @Test
    void showsParameterTooltipAfterOpeningBracket() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertEquals(new ShogiEffectSuggestionProvider.ParameterTooltip(List.of("value", "count"), 0), provider.parameterTooltip("custom_effect(", 14));
    }

    @Test
    void showsParameterTooltipAfterComma() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertEquals(new ShogiEffectSuggestionProvider.ParameterTooltip(List.of("value", "count"), 1), provider.parameterTooltip("custom_effect(1,", 16));
    }

    @Test
    void showsParameterTooltipAfterCommaAndWhitespace() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertEquals(new ShogiEffectSuggestionProvider.ParameterTooltip(List.of("value", "count"), 1), provider.parameterTooltip("custom_effect(1, ", 17));
    }

    @Test
    void showsParameterTooltipForNestedCurrentCall() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertEquals(new ShogiEffectSuggestionProvider.ParameterTooltip(List.of("value", "count"), 1), provider.parameterTooltip("noop(custom_effect(1,", 21));
    }

    @Test
    void ignoresNestedCommasForParameterTooltipIndex() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertEquals(new ShogiEffectSuggestionProvider.ParameterTooltip(List.of("value", "count"), 1), provider.parameterTooltip("custom_effect(noop(1, 2), ", 26));
    }

    @Test
    void doesNotShowParameterTooltipOutsideTriggerPositionOrStrings() {
        final var provider = new ShogiEffectSuggestionProvider(createScope());

        assertNull(provider.parameterTooltip("custom_effect(1", 15));
        assertNull(provider.parameterTooltip("'custom_effect(", 15));
        assertNull(provider.parameterTooltip("noop(", 5));
    }

    private static ShogiScopeImpl createScope() {
        final var scope = new ShogiScopeImpl(Identifier.fromNamespaceAndPath("shogi", "test"));
        scope.registerEffect(EmptyEffect.IDENTIFIER, EmptyEffect.MAP_CODEC);
        scope.registerEffect(Identifier.fromNamespaceAndPath("test", "custom"), EmptyEffect.MAP_CODEC);
        scope.registerEffect(Identifier.fromNamespaceAndPath("shogi", "custom_effect"), EmptyEffect.MAP_CODEC, List.of("value", "count"));
        scope.registerEffect(Identifier.fromNamespaceAndPath("shogi", "is_near"), EmptyEffect.MAP_CODEC);
        scope.registerEffect(Identifier.fromNamespaceAndPath("shogi", "is_near_block_entity"), EmptyEffect.MAP_CODEC);
        return scope;
    }
}
