package net.blay09.mods.shogi.client.gui;

import net.blay09.mods.shogi.common.util.ShogiExpressionHighlighter;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShogiExpressionHighlighterTest {
    @Test
    void highlightsShogiExpressionTokens() {
        final String input = "$uses_xp -> if(condition = true, then = add_hunger(-2), else = 'nope')";

        final List<ShogiExpressionHighlighter.Span> spans = ShogiExpressionHighlighter.highlight(input);

        assertColor(input, spans, "$uses_xp", ShogiExpressionHighlighter.VARIABLE_COLOR);
        assertColor(input, spans, "->", ShogiExpressionHighlighter.OPERATOR_COLOR);
        assertColor(input, spans, "if", ShogiExpressionHighlighter.IDENTIFIER_COLOR);
        assertColor(input, spans, "condition", ShogiExpressionHighlighter.PARAMETER_COLOR);
        assertColor(input, spans, "true", ShogiExpressionHighlighter.BOOLEAN_COLOR);
        assertColor(input, spans, "add_hunger", ShogiExpressionHighlighter.IDENTIFIER_COLOR);
        assertColor(input, spans, "2", ShogiExpressionHighlighter.NUMBER_COLOR);
        assertColor(input, spans, "'nope'", ShogiExpressionHighlighter.STRING_COLOR);
    }

    @Test
    void marksUnterminatedStringAsError() {
        final String input = "use('test:broken)";

        final List<ShogiExpressionHighlighter.Span> spans = ShogiExpressionHighlighter.highlight(input);

        assertColor(input, spans, "'test:broken)", ShogiExpressionHighlighter.ERROR_COLOR);
    }

    @Test
    void highlightsShogiExpressionAsComponent() {
        final String input = "$uses_xp -> if(condition = true, then = add_hunger(-2), else = 'nope')";

        final Component component = ShogiExpressionHighlighter.highlightComponent(input);

        assertEquals(input, component.getString());
        assertComponentColor(input, component, "$uses_xp", ShogiExpressionHighlighter.VARIABLE_COLOR);
        assertComponentColor(input, component, "->", ShogiExpressionHighlighter.OPERATOR_COLOR);
        assertComponentColor(input, component, "condition", ShogiExpressionHighlighter.PARAMETER_COLOR);
        assertComponentColor(input, component, "true", ShogiExpressionHighlighter.BOOLEAN_COLOR);
        assertComponentColor(input, component, "'nope'", ShogiExpressionHighlighter.STRING_COLOR);
    }

    private static void assertColor(String input, List<ShogiExpressionHighlighter.Span> spans, String token, int expectedColor) {
        final int start = input.indexOf(token);
        final int end = start + token.length();
        for (ShogiExpressionHighlighter.Span span : spans) {
            if (span.start() == start && span.end() == end) {
                assertEquals(expectedColor, span.color());
                return;
            }
        }

        throw new AssertionError("No span found for token: " + token);
    }

    private static void assertComponentColor(String input, Component component, String token, int expectedColor) {
        final int expectedStart = input.indexOf(token);
        int current = 0;
        for (Component sibling : component.getSiblings()) {
            final String text = sibling.getString();
            final int start = current;
            final int end = current + text.length();
            if (start == expectedStart && end == expectedStart + token.length()) {
                assertEquals(expectedColor & 0xFFFFFF, sibling.getStyle().getColor().getValue());
                return;
            }
            current = end;
        }

        throw new AssertionError("No component slice found for token: " + token);
    }
}
