package net.blay09.mods.shogi.common.scope;

import net.blay09.mods.shogi.effect.ShogiEffect;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public class ShogiRuleRepository {
    private Map<Identifier, ShogiEffect<?>> configRules = Map.of();
    private Map<Identifier, ShogiEffect<?>> datapackRules = Map.of();

    public void apply(Map<Identifier, ShogiEffect<?>> valueOverrides, Map<Identifier, ShogiEffect<?>> importedRules) {
        this.configRules = Map.copyOf(valueOverrides);
        this.datapackRules = Map.copyOf(importedRules);
    }

    public Optional<ShogiEffect<?>> getValueOverride(Identifier identifier) {
        return Optional.ofNullable(configRules.get(identifier));
    }

    public Optional<ShogiEffect<?>> getImportedRule(Identifier identifier) {
        return Optional.ofNullable(datapackRules.get(identifier));
    }
}
