package net.blay09.mods.shogi.client.gui;

import net.blay09.mods.shogi.common.parse.DefaultedIdentifiers;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ShogiEffectSuggestionProvider {
    private final ShogiScope scope;

    public ShogiEffectSuggestionProvider(ShogiScope scope) {
        this.scope = scope;
    }

    @Nullable
    public TextSuggestions suggest(String value, int cursor) {
        final SuggestionContext context = findSuggestionContext(value, cursor);
        if (context == null) {
            return null;
        }
        if (context.prefix().isEmpty()) {
            return null;
        }

        final List<String> suggestions = collectSuggestions(context.prefix());
        return suggestions.isEmpty() ? null : new TextSuggestions(context.start(), context.end(), suggestions);
    }

    @Nullable
    public ParameterTooltip parameterTooltip(String value, int cursor) {
        final ParameterTooltipContext context = findParameterTooltipContext(value, cursor);
        if (context == null) {
            return null;
        }

        final List<String> parameters = scope.getOrdinalParameters(context.identifier());
        return parameters.isEmpty() ? null : new ParameterTooltip(parameters, context.parameterIndex());
    }

    private List<String> collectSuggestions(String prefix) {
        final Set<String> names = new LinkedHashSet<>();
        final boolean qualifiedPrefix = prefix.indexOf(':') >= 0;
        final List<String> defaultNamespaces = scope.getDefaultNamespaces();
        for (Identifier identifier : scope.getEffectIdentifiers()) {
            final String qualifiedName = identifier.toString();
            if (qualifiedPrefix) {
                addIfMatching(names, qualifiedName, prefix);
            } else if (defaultNamespaces.contains(identifier.getNamespace())) {
                addIfMatching(names, identifier.getPath(), prefix);
            } else {
                addIfMatching(names, qualifiedName, prefix);
            }
        }
        return new ArrayList<>(names);
    }

    private static void addIfMatching(Set<String> names, String value, String prefix) {
        if (value.startsWith(prefix) && !value.equals(prefix)) {
            names.add(value);
        }
    }

    @Nullable
    private static SuggestionContext findSuggestionContext(String value, int cursor) {
        if (cursor > value.length() || isInsideString(value, cursor)) {
            return null;
        }

        int start = cursor;
        while (start > 0 && isCallIdentifierPart(value.charAt(start - 1))) {
            start--;
        }
        if (start > 0 && value.charAt(start - 1) == '$') {
            return null;
        }

        int end = cursor;
        while (end < value.length() && isCallIdentifierPart(value.charAt(end))) {
            end++;
        }
        if (start == end && !canStartEffectName(value, cursor)) {
            return null;
        }
        if (start < end && !isIdentifierStart(value.charAt(start))) {
            return null;
        }
        return new SuggestionContext(start, end, value.substring(start, cursor));
    }

    private static boolean canStartEffectName(String value, int cursor) {
        if (cursor == 0) {
            return true;
        }

        final char previous = value.charAt(cursor - 1);
        return Character.isWhitespace(previous) || previous == '(' || previous == ',' || previous == '+' || previous == '-' || previous == '>' || previous == '=';
    }

    @Nullable
    private ParameterTooltipContext findParameterTooltipContext(String value, int cursor) {
        if (cursor == 0 || cursor > value.length() || isInsideString(value, cursor)) {
            return null;
        }

        int triggerIndex = cursor - 1;
        while (triggerIndex >= 0 && Character.isWhitespace(value.charAt(triggerIndex))) {
            triggerIndex--;
        }

        if (triggerIndex < 0) {
            return null;
        }

        final char previous = value.charAt(triggerIndex);
        if (previous != '(' && previous != ',') {
            return null;
        }

        final int openingBracket = previous == '(' ? triggerIndex : findEnclosingOpeningBracket(value, triggerIndex);
        if (openingBracket <= 0) {
            return null;
        }

        int identifierEnd = openingBracket;
        while (identifierEnd > 0 && Character.isWhitespace(value.charAt(identifierEnd - 1))) {
            identifierEnd--;
        }

        int identifierStart = identifierEnd;
        while (identifierStart > 0 && isCallIdentifierPart(value.charAt(identifierStart - 1))) {
            identifierStart--;
        }

        if (identifierStart == identifierEnd || !isIdentifierStart(value.charAt(identifierStart))) {
            return null;
        }

        final String identifier = value.substring(identifierStart, identifierEnd);
        final Identifier resolvedIdentifier = DefaultedIdentifiers.parse(identifier, scope.getDefaultNamespaces(), scope::hasEffect);
        if (resolvedIdentifier == null) {
            return null;
        }

        return new ParameterTooltipContext(resolvedIdentifier, countParameterIndex(value, openingBracket + 1, triggerIndex));
    }

    private static int countParameterIndex(String value, int start, int end) {
        int parameterIndex = 0;
        int depth = 0;
        char quote = 0;
        for (int i = start; i <= end; i++) {
            final char ch = value.charAt(i);
            if (quote != 0) {
                if (ch == '\\') {
                    i++;
                } else if (ch == quote) {
                    quote = 0;
                }
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            } else if (ch == '(') {
                depth++;
            } else if (ch == ')' && depth > 0) {
                depth--;
            } else if (ch == ',' && depth == 0) {
                parameterIndex++;
            }
        }
        return parameterIndex;
    }

    private static int findEnclosingOpeningBracket(String value, int end) {
        final Deque<Integer> openingBrackets = new ArrayDeque<>();
        char quote = 0;
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            if (quote != 0) {
                if (ch == '\\') {
                    i++;
                } else if (ch == quote) {
                    quote = 0;
                }
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            } else if (ch == '(') {
                openingBrackets.push(i);
            } else if (ch == ')' && !openingBrackets.isEmpty()) {
                openingBrackets.pop();
            }
        }

        return openingBrackets.isEmpty() ? -1 : openingBrackets.peek();
    }

    private static boolean isInsideString(String value, int cursor) {
        char quote = 0;
        for (int i = 0; i < cursor; i++) {
            final char ch = value.charAt(i);
            if (quote != 0) {
                if (ch == '\\') {
                    i++;
                } else if (ch == quote) {
                    quote = 0;
                }
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            }
        }
        return quote != 0;
    }

    private static boolean isIdentifierStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private static boolean isIdentifierPart(char ch) {
        return isIdentifierStart(ch) || (ch >= '0' && ch <= '9');
    }

    private static boolean isCallIdentifierPart(char ch) {
        return isIdentifierPart(ch) || ch == ':' || ch == '/' || ch == '.' || ch == '-';
    }

    private record SuggestionContext(int start, int end, String prefix) {
    }

    private record ParameterTooltipContext(Identifier identifier, int parameterIndex) {
    }

    public record ParameterTooltip(List<String> parameters, int parameterIndex) {
    }
}
