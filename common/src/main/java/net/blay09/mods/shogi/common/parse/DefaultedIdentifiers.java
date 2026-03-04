package net.blay09.mods.shogi.common.parse;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DefaultedIdentifiers {
    @Nullable
    public static Identifier parse(String identifier, String defaultNamespace) {
        return parse(identifier, List.of(defaultNamespace), it -> true);
    }

    @Nullable
    public static Identifier parse(String identifier, List<String> defaultNamespaces) {
        return parse(identifier, defaultNamespaces, _ -> true);
    }

    @Nullable
    public static Identifier parse(String identifier, List<String> defaultNamespaces, Predicate<Identifier> matcher) {
        final var separatorIndex = identifier.indexOf(':');
        if (separatorIndex >= 0) {
            final var path = identifier.substring(separatorIndex + 1);
            if (!Identifier.isValidPath(path)) {
                return null;
            } else if (separatorIndex != 0) {
                final String namespace = identifier.substring(0, separatorIndex);
                if (!Identifier.isValidNamespace(namespace)) {
                    return null;
                }
                final var resolved = Identifier.fromNamespaceAndPath(namespace, path);
                return matcher.test(resolved) ? resolved : null;
            } else {
                return firstMatching(path, defaultNamespaces, matcher);
            }
        } else {
            return Identifier.isValidPath(identifier) ? firstMatching(identifier, defaultNamespaces, matcher) : null;
        }
    }

    @Nullable
    private static Identifier firstMatching(String path, List<String> defaultNamespaces, Predicate<Identifier> matcher) {
        Identifier firstValid = null;
        for (final var namespace : defaultNamespaces) {
            if (!Identifier.isValidNamespace(namespace)) {
                continue;
            }

            final var resolved = Identifier.fromNamespaceAndPath(namespace, path);
            if (firstValid == null) {
                firstValid = resolved;
            }
            if (matcher.test(resolved)) {
                return resolved;
            }
        }
        return firstValid;
    }
}
