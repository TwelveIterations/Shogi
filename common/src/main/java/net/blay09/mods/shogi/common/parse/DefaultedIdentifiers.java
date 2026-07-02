package net.blay09.mods.shogi.common.parse;

import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DefaultedIdentifiers {
    @Nullable
    public static ResourceLocation parse(String identifier, String defaultNamespace) {
        return parse(identifier, List.of(defaultNamespace), it -> true);
    }

    @Nullable
    public static ResourceLocation parse(String identifier, List<String> defaultNamespaces) {
        return parse(identifier, defaultNamespaces, ignored -> true);
    }

    @Nullable
    public static ResourceLocation parse(String identifier, List<String> defaultNamespaces, Predicate<ResourceLocation> matcher) {
        final var separatorIndex = identifier.indexOf(':');
        if (separatorIndex >= 0) {
            final var path = identifier.substring(separatorIndex + 1);
            if (!ResourceLocation.isValidPath(path)) {
                return null;
            } else if (separatorIndex != 0) {
                final String namespace = identifier.substring(0, separatorIndex);
                if (!ResourceLocation.isValidNamespace(namespace)) {
                    return null;
                }
                final var resolved = ResourceLocation.fromNamespaceAndPath(namespace, path);
                return matcher.test(resolved) ? resolved : null;
            } else {
                return firstMatching(path, defaultNamespaces, matcher);
            }
        } else {
            return ResourceLocation.isValidPath(identifier) ? firstMatching(identifier, defaultNamespaces, matcher) : null;
        }
    }

    @Nullable
    private static ResourceLocation firstMatching(String path, List<String> defaultNamespaces, Predicate<ResourceLocation> matcher) {
        for (final var namespace : defaultNamespaces) {
            if (!ResourceLocation.isValidNamespace(namespace)) {
                continue;
            }

            final var resolved = ResourceLocation.fromNamespaceAndPath(namespace, path);
            if (matcher.test(resolved)) {
                return resolved;
            }
        }
        return null;
    }
}
