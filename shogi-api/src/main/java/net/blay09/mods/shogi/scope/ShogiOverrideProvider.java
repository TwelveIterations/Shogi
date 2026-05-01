package net.blay09.mods.shogi.scope;

import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Provider for externally supplied value overrides.
 */
public interface ShogiOverrideProvider {
    /**
     * Resolves an override effect for the given value identifier.
     *
     * @param identifier value identifier
     * @return override effect, or empty if none applies
     */
    Optional<ShogiEffect<?>> getOverride(Identifier identifier);
}
