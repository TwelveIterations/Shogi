package net.blay09.mods.shogi.client.gui;

import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
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
}
