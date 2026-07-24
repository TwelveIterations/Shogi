package net.blay09.mods.shogi.client.gui;

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
}
