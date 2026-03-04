package net.blay09.mods.shogi.common.parse;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class DefaultedIdentifiers {
    @Nullable
    public static Identifier parse(String identifier, String defaultNamespace) {
        final var separatorIndex = identifier.indexOf(':');
        if (separatorIndex >= 0) {
            final var path = identifier.substring(separatorIndex + 1);
            if (!Identifier.isValidPath(path)) {
                return null;
            } else if (separatorIndex != 0) {
                String namespace = identifier.substring(0, separatorIndex);
                return Identifier.isValidNamespace(namespace) ? Identifier.fromNamespaceAndPath(namespace, path) : null;
            } else {
                return Identifier.fromNamespaceAndPath(defaultNamespace, path);
            }
        } else {
            return Identifier.isValidPath(identifier) ? Identifier.fromNamespaceAndPath(defaultNamespace, identifier) : null;
        }
    }
}
