package net.blay09.mods.shogi.common.scope;

import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiOverrideProvider;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public class ShogiOverrideProviderImpl implements ShogiOverrideProvider {
    private Map<Identifier, ShogiEffect<?>> overrides = Map.of();

    public void apply(Map<Identifier, ShogiEffect<?>> overrides) {
        this.overrides = overrides;
    }

    @Override
    public Optional<ShogiEffect<?>> getOverride(Identifier identifier) {
        return Optional.ofNullable(overrides.get(identifier));
    }
}
