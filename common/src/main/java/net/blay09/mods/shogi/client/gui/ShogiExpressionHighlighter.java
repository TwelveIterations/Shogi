package net.blay09.mods.shogi.client.gui;

import java.util.ArrayList;
import java.util.List;

public class ShogiExpressionHighlighter {
    public static final int DEFAULT_COLOR = 0xFFE0E0E0;
    public static final int VARIABLE_COLOR = 0xFF8FD6FF;
    public static final int STRING_COLOR = 0xFFFFC66D;
    public static final int NUMBER_COLOR = 0xFFB5E48C;
    public static final int BOOLEAN_COLOR = 0xFFD6A3FF;
    public static final int IDENTIFIER_COLOR = 0xFFF1F5A9;
    public static final int PARAMETER_COLOR = 0xFF7DD3C7;
    public static final int OPERATOR_COLOR = 0xFFFF8F8F;
    public static final int PUNCTUATION_COLOR = 0xFFB6BCC6;
    public static final int ERROR_COLOR = 0xFFFF5555;

    public static List<Span> highlight(String input) {
        final List<Span> spans = new ArrayList<>();
        int pos = 0;
        while (pos < input.length()) {
            final char ch = input.charAt(pos);
            if (Character.isWhitespace(ch)) {
                pos++;
                continue;
            }

            if (ch == '$') {
                pos = readVariable(input, spans, pos);
                continue;
            }

            if (ch == '\'' || ch == '"') {
                pos = readString(input, spans, pos);
                continue;
            }

            if (isDigit(ch)) {
                pos = readNumber(input, spans, pos);
                continue;
            }

            if (isIdentifierStart(ch)) {
                pos = readIdentifier(input, spans, pos);
                continue;
            }

            if (ch == '-' && peek(input, pos + 1) == '>') {
                spans.add(new Span(pos, pos + 2, OPERATOR_COLOR));
                pos += 2;
                continue;
            }

            if (isOperator(ch)) {
                spans.add(new Span(pos, pos + 1, OPERATOR_COLOR));
            } else if (isPunctuation(ch)) {
                spans.add(new Span(pos, pos + 1, PUNCTUATION_COLOR));
            } else {
                spans.add(new Span(pos, pos + 1, ERROR_COLOR));
            }
            pos++;
        }

        return spans;
    }

    private static int readVariable(String input, List<Span> spans, int start) {
        int pos = start + 1;
        if (!isIdentifierStart(peek(input, pos))) {
            spans.add(new Span(start, pos, ERROR_COLOR));
            return pos;
        }

        pos = readSimpleIdentifier(input, pos);
        while (peek(input, pos) == '.' && isIdentifierStart(peek(input, pos + 1))) {
            pos = readSimpleIdentifier(input, pos + 1);
        }

        spans.add(new Span(start, pos, VARIABLE_COLOR));
        return pos;
    }

    private static int readString(String input, List<Span> spans, int start) {
        final char quote = input.charAt(start);
        int pos = start + 1;
        boolean closed = false;
        while (pos < input.length()) {
            final char ch = input.charAt(pos++);
            if (ch == quote) {
                closed = true;
                break;
            }
            if (ch == '\\' && pos < input.length()) {
                pos++;
            }
        }

        spans.add(new Span(start, pos, closed ? STRING_COLOR : ERROR_COLOR));
        return pos;
    }

    private static int readNumber(String input, List<Span> spans, int start) {
        int pos = start;
        while (isDigit(peek(input, pos))) {
            pos++;
        }

        if (peek(input, pos) == '.') {
            pos++;
            while (isDigit(peek(input, pos))) {
                pos++;
            }
        }

        spans.add(new Span(start, pos, NUMBER_COLOR));
        return pos;
    }

    private static int readIdentifier(String input, List<Span> spans, int start) {
        int pos = start + 1;
        while (isCallIdentifierPart(peek(input, pos)) && !(peek(input, pos) == '-' && peek(input, pos + 1) == '>')) {
            pos++;
        }

        final String value = input.substring(start, pos);
        if ("true".equals(value) || "false".equals(value)) {
            spans.add(new Span(start, pos, BOOLEAN_COLOR));
        } else if (isNamedArgument(input, pos)) {
            spans.add(new Span(start, pos, PARAMETER_COLOR));
        } else {
            spans.add(new Span(start, pos, IDENTIFIER_COLOR));
        }
        return pos;
    }

    private static int readSimpleIdentifier(String input, int start) {
        int pos = start + 1;
        while (isIdentifierPart(peek(input, pos))) {
            pos++;
        }
        return pos;
    }

    private static boolean isNamedArgument(String input, int pos) {
        while (Character.isWhitespace(peek(input, pos))) {
            pos++;
        }
        return peek(input, pos) == '=' && peek(input, pos + 1) != '=' && !(peek(input, pos - 1) == '#');
    }

    private static char peek(String input, int pos) {
        return pos >= 0 && pos < input.length() ? input.charAt(pos) : '\0';
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isIdentifierStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private static boolean isIdentifierPart(char ch) {
        return isIdentifierStart(ch) || isDigit(ch);
    }

    private static boolean isCallIdentifierPart(char ch) {
        return isIdentifierPart(ch) || ch == ':' || ch == '/' || ch == '.' || ch == '-';
    }

    private static boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '!' || ch == '=';
    }

    private static boolean isPunctuation(char ch) {
        return ch == '(' || ch == ')' || ch == ',' || ch == '#';
    }

    public record Span(int start, int end, int color) {
    }
}
